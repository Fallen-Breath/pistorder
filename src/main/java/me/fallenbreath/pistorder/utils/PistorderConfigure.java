package me.fallenbreath.pistorder.utils;

import me.fallenbreath.pistorder.PistorderMod;
import net.minecraft.client.util.InputUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PistorderConfigure
{
	private static final String CONFIG_FILE_PATH = String.format("./config/%s.properties", PistorderMod.MOD_ID);

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
			save();
		}
	}

	public static void save()
	{
		try
		{
			writeConfig();
			PistorderMod.LOGGER.info("Configure file saved");
		}
		catch (IOException e)
		{
			PistorderMod.LOGGER.error("Failed to save configure: " + e);
		}
	}

	private static void readConfig() throws IOException
	{
		File file = new File(CONFIG_FILE_PATH);
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		loadProperties(properties);
	}

	private static void loadProperties(Properties properties)
	{
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();

			if (key.equals("keybinding_clear"))
			{
				PistorderKeyBinding.CLEAR_DISPLAY_KEY.setKeyCode(InputUtil.fromName(value));
			}
		}
	}

	private static void writeConfig() throws IOException
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

		dumpProperties().store(new FileOutputStream(file), String.format("Configure file for %s", PistorderMod.MOD_NAME));
	}

	private static Properties dumpProperties()
	{
		Properties properties = new Properties();

		properties.put("keybinding_clear", PistorderKeyBinding.CLEAR_DISPLAY_KEY.getName());

		return properties;
	}
}
