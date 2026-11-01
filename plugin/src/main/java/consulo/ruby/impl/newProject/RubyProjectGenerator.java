package consulo.ruby.impl.newProject;

import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.ruby.impl.bundler.BundlerProcessRunner;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Writes or generates the files of a new Ruby project.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class RubyProjectGenerator {
    private static final int OUTPUT_TAIL_LINES = 20;

    private RubyProjectGenerator() {
    }

    /**
     * @return the file to open once the project is imported
     */
    public static Path getEntryFile(RubyProjectTemplate template, Path dir) {
        return switch (template) {
            case APPLICATION -> dir.resolve("main.rb");
            case GEM -> {
                Path lib = dir.resolve("lib").resolve(dir.getFileName().toString().replace('-', '/') + ".rb");
                yield Files.exists(lib) ? lib : dir.resolve("README.md");
            }
            case RAILS -> dir.resolve("config").resolve("routes.rb");
        };
    }

    public static void writeApplication(Path dir, String name) throws IOException {
        String fileName = toFileName(name);
        String moduleName = toModuleName(fileName);

        write(dir.resolve("Gemfile"), """
            # frozen_string_literal: true

            source "https://rubygems.org"

            gem "rake"

            group :test do
              gem "minitest"
            end
            """);
        write(dir.resolve("Rakefile"), """
            # frozen_string_literal: true

            require "rake/testtask"

            Rake::TestTask.new(:test) do |t|
              t.libs << "lib" << "test"
              t.test_files = FileList["test/**/*_test.rb"]
            end

            task default: :test
            """);
        write(dir.resolve("main.rb"), """
            # frozen_string_literal: true

            require_relative "lib/%s"

            puts %s.greet("World")
            """.formatted(fileName, moduleName));
        write(dir.resolve("lib").resolve(fileName + ".rb"), """
            # frozen_string_literal: true

            module %s
              def self.greet(name) = "Hello, #{name}!"
            end
            """.formatted(moduleName));
        write(dir.resolve("test").resolve(fileName + "_test.rb"), """
            # frozen_string_literal: true

            require "minitest/autorun"
            require_relative "../lib/%s"

            class %sTest < Minitest::Test
              def test_greet
                assert_equal "Hello, Ruby!", %s.greet("Ruby")
              end
            end
            """.formatted(fileName, moduleName, moduleName));
    }

    /**
     * Runs {@code bundle gem} or {@code rails new} and refreshes the generated files.
     *
     * @return null on success, else the reason with the end of the output
     */
    @Nullable
    public static String generate(RubyProjectTemplate template, Path dir, String rubyExecutable) {
        List<String> parameters = new ArrayList<>();
        Path workDir;
        switch (template) {
            case GEM -> {
                workDir = dir.getParent();
                parameters.addAll(BundlerProcessRunner.gemExecutable("bundler", "bundle"));
                parameters.addAll(List.of("gem", dir.getFileName().toString(),
                    "--test=minitest", "--no-coc", "--no-mit", "--no-ci", "--no-linter", "--no-git"));
            }
            case RAILS -> {
                workDir = dir;
                parameters.addAll(BundlerProcessRunner.gemExecutable("railties", "rails"));
                parameters.addAll(List.of("new", ".", "--skip-git"));
            }
            default -> {
                return null;
            }
        }

        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.withExePath(rubyExecutable);
        commandLine.withWorkingDirectory(workDir);
        commandLine.withParameters(parameters);

        StringBuilder output = new StringBuilder();
        String error = null;
        try {
            int exitCode = new BundlerProcessRunner().run(commandLine, (text, stdout) -> output.append(text), 0);
            if (exitCode != 0) {
                error = commandLine.getCommandLineString() + " (" + exitCode + ")\n" + tail(output.toString());
            }
        }
        catch (ExecutionException e) {
            error = e.getMessage();
        }

        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(dir);
        if (root != null) {
            root.refresh(false, true);
        }
        return error;
    }

    static String toFileName(String name) {
        String fileName = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").replaceAll("[^A-Za-z0-9]+", "_").toLowerCase(Locale.ROOT);
        fileName = fileName.replaceAll("^_+|_+$", "");
        if (fileName.isEmpty()) {
            return "app";
        }
        return Character.isDigit(fileName.charAt(0)) ? "app_" + fileName : fileName;
    }

    static String toModuleName(String fileName) {
        StringBuilder builder = new StringBuilder();
        for (String part : fileName.split("_")) {
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private static void write(Path file, String text) throws IOException {
        if (Files.exists(file)) {
            return;
        }
        Files.createDirectories(file.getParent());
        Files.writeString(file, text);
    }

    private static String tail(String output) {
        String[] lines = output.split("\\R");
        int from = Math.max(0, lines.length - OUTPUT_TAIL_LINES);
        return String.join("\n", List.of(lines).subList(from, lines.length));
    }
}
