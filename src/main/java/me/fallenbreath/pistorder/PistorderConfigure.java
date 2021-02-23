package me.fallenbreath.pistorder;

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
			PistorderMod.LOGGER.error(String.format("Configure file for %s loaded", PistorderMod.MOD_NAME));
		}
		catch (IOException e)
		{
			PistorderMod.LOGGER.error("Fail to load configure: " + e);
			PistorderMod.LOGGER.error("Use default configure");
		}
	}

	public static void save()
	{
		try
		{
			writeConfig();
		}
		catch (IOException e)
		{
			PistorderMod.LOGGER.error("Fail to save configure: " + e);
			PistorderMod.LOGGER.error("Use default configure");
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

			if (key.equals("key_" + PistorderKeyBinding.CLEAR_DISPLAY_KEY.getId()))
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

		properties.put("key_" + PistorderKeyBinding.CLEAR_DISPLAY_KEY.getId(), PistorderKeyBinding.CLEAR_DISPLAY_KEY.getName());

		return properties;
	}
}
