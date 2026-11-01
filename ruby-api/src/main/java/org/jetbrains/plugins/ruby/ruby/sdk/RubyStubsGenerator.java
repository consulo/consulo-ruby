package org.jetbrains.plugins.ruby.ruby.sdk;

import consulo.container.boot.ContainerPathManager;
import consulo.logging.Logger;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.run.Output;
import org.jetbrains.plugins.ruby.ruby.run.Runner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Generates the core library stubs of an interpreter from the RBS signatures it ships (Ruby 3.0+), into the system
 * directory, once per interpreter and version.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class RubyStubsGenerator
{
	private static final Logger LOG = Logger.getInstance(RubyStubsGenerator.class);

	private static final String SCRIPT = "/ruby-stubs/generate_stubs.rb";
	/**
	 * Changes with the output of the script, so older stubs are generated again
	 */
	private static final String SCRIPT_VERSION = "2";

	private RubyStubsGenerator()
	{
	}

	/**
	 * @return the stubs directory (named {@code rubystubs}), or null when the interpreter has no RBS signatures
	 */
	@Nullable
	public static Path generate(@Nonnull final String rubyExecutable, @Nullable final String versionString)
	{
		final String key = rubyExecutable + "|" + versionString + "|" + SCRIPT_VERSION;
		final Path root = Path.of(ContainerPathManager.get().getSystemPath(), "ruby-stubs", UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString());
		final Path stubsDirectory = root.resolve(RubySdkType.RUBYSTUBS_DIR);
		if(Files.isRegularFile(stubsDirectory.resolve("builtin.rb")))
		{
			return stubsDirectory;
		}

		try
		{
			delete(root);
			Files.createDirectories(root);
			final Path script = root.resolve("generate_stubs.rb");
			try(InputStream stream = RubyStubsGenerator.class.getResourceAsStream(SCRIPT))
			{
				if(stream == null)
				{
					LOG.error("No " + SCRIPT);
					return null;
				}
				Files.write(script, stream.readAllBytes());
			}

			final Output output = Runner.run(rubyExecutable, script.toString(), stubsDirectory.toString());
			if(Files.isRegularFile(stubsDirectory.resolve("builtin.rb")))
			{
				return stubsDirectory;
			}
			LOG.warn("Cannot generate the stubs of " + rubyExecutable + ": " + output.getStderr());
		}
		catch(IOException e)
		{
			LOG.warn("Cannot generate the stubs of " + rubyExecutable, e);
		}
		delete(root);
		return null;
	}

	private static void delete(Path directory)
	{
		if(!Files.exists(directory))
		{
			return;
		}
		try(Stream<Path> walk = Files.walk(directory))
		{
			walk.sorted(Comparator.reverseOrder()).forEach(path -> {
				try
				{
					Files.deleteIfExists(path);
				}
				catch(IOException ignored)
				{
				}
			});
		}
		catch(IOException e)
		{
			LOG.warn("Cannot delete " + directory, e);
		}
	}
}
