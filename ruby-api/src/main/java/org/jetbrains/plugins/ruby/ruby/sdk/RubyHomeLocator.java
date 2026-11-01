package org.jetbrains.plugins.ruby.ruby.sdk;

import consulo.logging.Logger;
import consulo.platform.Platform;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Finds Ruby installations: the PATH, the system and package manager locations and the version managers
 * (rbenv, rvm, asdf, mise, chruby, frum). A home is a directory with {@code bin/<executable>}.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class RubyHomeLocator
{
	private static final Logger LOG = Logger.getInstance(RubyHomeLocator.class);

	private RubyHomeLocator()
	{
	}

	/**
	 * The active Ruby of chruby and rvm - the platform makes an SDK named after each variable
	 */
	public static final Set<String> HOME_ENVIRONMENT_VARIABLES = Set.of("RUBY_ROOT", "MY_RUBY_HOME");

	public static void collect(@Nonnull Platform platform, @Nonnull String executable, @Nonnull Consumer<Path> consumer)
	{
		Set<Path> homes = new LinkedHashSet<>();

		collectFromPath(platform, executable, homes);

		Path userHome = platform.user().homePath();
		if(platform.os().isWindows())
		{
			// RubyInstaller: C:\Ruby34-x64
			for(Path root : platform.fs().getRootDirectories())
			{
				collectChildren(root, name -> name.toLowerCase().startsWith("ruby"), homes);
			}
		}
		else
		{
			homes.add(platform.fs().getPath("/usr"));
			homes.add(platform.fs().getPath("/usr/local"));
			homes.add(platform.fs().getPath("/opt/homebrew/opt/ruby"));
			homes.add(platform.fs().getPath("/usr/local/opt/ruby"));
			homes.add(platform.fs().getPath("/opt/local"));
			collectChildren(platform.fs().getPath("/opt/rubies"), name -> true, homes);
			collectChildren(platform.fs().getPath("/usr/local/rvm/rubies"), name -> true, homes);
		}

		collectChildren(dataDir(platform, "RBENV_ROOT", userHome.resolve(".rbenv")).resolve("versions"), name -> true, homes);
		collectChildren(userHome.resolve(".rvm").resolve("rubies"), name -> true, homes);
		collectChildren(dataDir(platform, "ASDF_DATA_DIR", userHome.resolve(".asdf")).resolve("installs").resolve("ruby"), name -> true, homes);
		collectChildren(dataDir(platform, "MISE_DATA_DIR", userHome.resolve(".local").resolve("share").resolve("mise")).resolve("installs").resolve("ruby"), name -> true, homes);
		collectChildren(userHome.resolve(".rubies"), name -> true, homes);
		collectChildren(userHome.resolve(".frum").resolve("versions"), name -> true, homes);

		Set<Path> seen = new LinkedHashSet<>();
		// the homes of the environment variables are SDKs of their own
		for(String variable : HOME_ENVIRONMENT_VARIABLES)
		{
			String value = platform.os().getEnvironmentVariable(variable);
			if(value != null && !value.isBlank())
			{
				seen.add(realPath(platform.fs().getPath(value)));
			}
		}
		for(Path home : homes)
		{
			if(!Files.isRegularFile(home.resolve("bin").resolve(executable)))
			{
				continue;
			}
			// /usr and a PATH entry may be the same installation
			if(seen.add(realPath(home)))
			{
				consumer.accept(home);
			}
		}
	}

	/**
	 * @return where the installation comes from, for the SDK name
	 */
	@Nullable
	public static String getManagerName(@Nonnull Path home)
	{
		// <root>/versions/<version> next to <root>/shims - an rbenv root anywhere (RBENV_ROOT)
		Path versions = home.getParent();
		if(versions != null && versions.getFileName() != null && "versions".equals(versions.getFileName().toString())
				&& versions.getParent() != null && Files.isDirectory(versions.getParent().resolve("shims")))
		{
			return "rbenv";
		}

		String path = home.toString().replace('\\', '/');
		for(String[] manager : List.of(
				new String[]{"/.rbenv/", "rbenv"},
				new String[]{"/.rvm/", "rvm"},
				new String[]{"/rvm/rubies/", "rvm"},
				new String[]{"/.asdf/", "asdf"},
				new String[]{"/mise/installs/", "mise"},
				new String[]{"/.rubies/", "chruby"},
				new String[]{"/opt/rubies/", "chruby"},
				new String[]{"/.frum/", "frum"},
				new String[]{"/homebrew/", "Homebrew"},
				new String[]{"/usr/local/opt/", "Homebrew"}))
		{
			if(path.contains(manager[0]))
			{
				return manager[1];
			}
		}
		return null;
	}

	private static void collectFromPath(Platform platform, String executable, Set<Path> homes)
	{
		String pathVariable = platform.os().getEnvironmentVariable("PATH");
		if(pathVariable == null)
		{
			return;
		}
		String separator = platform.os().isWindows() ? ";" : ":";
		for(String entry : pathVariable.split(separator))
		{
			if(entry.isBlank())
			{
				continue;
			}
			Path bin = platform.fs().getPath(entry);
			Path parent = bin.getParent();
			if(parent != null && bin.getFileName() != null && "bin".equals(bin.getFileName().toString()) && Files.isRegularFile(bin.resolve(executable)))
			{
				homes.add(parent);
			}
		}
	}

	private static Path dataDir(Platform platform, String variable, Path fallback)
	{
		String value = platform.os().getEnvironmentVariable(variable);
		return value == null || value.isBlank() ? fallback : platform.fs().getPath(value);
	}

	private static void collectChildren(Path directory, java.util.function.Predicate<String> nameFilter, Set<Path> homes)
	{
		if(!Files.isDirectory(directory))
		{
			return;
		}
		try(Stream<Path> children = Files.list(directory))
		{
			children.filter(Files::isDirectory)
					.filter(child -> child.getFileName() != null && nameFilter.test(child.getFileName().toString()))
					.sorted()
					.forEach(homes::add);
		}
		catch(IOException e)
		{
			LOG.warn("Cannot list " + directory, e);
		}
	}

	private static Path realPath(Path path)
	{
		try
		{
			return path.toRealPath();
		}
		catch(IOException e)
		{
			return path.toAbsolutePath().normalize();
		}
	}
}
