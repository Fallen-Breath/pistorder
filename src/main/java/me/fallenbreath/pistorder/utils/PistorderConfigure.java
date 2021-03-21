package me.fallenbreath.pistorder.utils;

import com.google.common.collect.Maps;
import me.fallenbreath.pistorder.PistorderMod;
import net.minecraft.client.util.InputUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PistorderConfigure
{
	private static final String CONFIG_FILE_PATH = String.format("./config/%s.properties", PistorderMod.MOD_ID);

	private static final Map<String, ConfigureElement> elements = Maps.newHashMap();

	public static boolean SWING_HAND = true;
	public static boolean DYNAMICALLY_INFORMATION_UPDATE = true;

	private static void register(String key, Consumer<String> reader, Supplier<String> getter)
	{
		elements.put(key, new ConfigureElement(key, reader, getter));
	}

	static
	{
		register(
				"keybinding_clear",
				v -> PistorderKeyBinding.CLEAR_DISPLAY_KEY.setBoundKey(InputUtil.fromTranslationKey(v)),
				PistorderKeyBinding.CLEAR_DISPLAY_KEY::getBoundKeyTranslationKey
		);
		register(
				"swing_hand",
				v -> SWING_HAND = Boolean.parseBoolean(v),
				() -> String.valueOf(SWING_HAND)
		);
		register(
				"dynamically_information_update",
				v -> DYNAMICALLY_INFORMATION_UPDATE = Boolean.parseBoolean(v),
				() -> String.valueOf(DYNAMICALLY_INFORMATION_UPDATE)
		);
	}

	public static void load()
	{
		try
		{
			readConfig();
			PistorderMod.LOGGER.info("Configure file loaded");
		}
		catch (IOException e)
		{
			PistorderMod.LOGGER.error("Failed to load configure: " + e);
			PistorderMod.LOGGER.error("Use default configure");
		}
	}

	public static void save()
	{
		try
		{
			writeConfig();
			PistorderMod.LOGGER.debug("Configure file saved");
		}
		catch (IOException e)
		{
			PistorderMod.LOGGER.error("Failed to save configure: " + e);
		}
	}

	synchronized private static void readConfig() throws IOException
	{
		File file = new File(CONFIG_FILE_PATH);
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		properties.forEach((key, value) -> {
			ConfigureElement element = elements.get((String)key);
			if (element != null)
			{
				element.reader.accept((String)value);
			}
		});
	}

	synchronized private static void writeConfig() throws IOException
	{
		File file = new File(CONFIG_FILE_PATH);
		File dir = file.getParentFile();

		if (!dir.exists())
		{
			if (!dir.mkdirs())
			{
				throw new IOException("Config dir creation failed");
			}
		}
		else if (!dir.isDirectory())
		{
			throw new IOException("Config dir is not a directory");
		}
		Properties properties = new Properties();
		elements.values().forEach(e -> properties.put(e.key, e.writer.get()));
		properties.store(new FileOutputStream(file), String.format("Configure file for %s", PistorderMod.MOD_NAME));
	}

	private static class ConfigureElement
	{
		private final String key;
		private final Consumer<String> reader;
		private final Supplier<String> writer;

		private ConfigureElement(String key, Consumer<String> reader, Supplier<String> writer)
		{
			this.key = key;
			this.reader = reader;
			this.writer = writer;
		}
	}
}
