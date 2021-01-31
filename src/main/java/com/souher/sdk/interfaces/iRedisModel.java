package com.souher.sdk.interfaces;

import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.iRedis;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public interface iRedisModel
{
    default String[] keyColumn()
    {
        return new String[]{"id"};
    }

    default void removeFromRedis()
    {
        try
        {
            DataResult<String> usedKey = new DataResult<>();
            for (int i = 0; i < keyColumn().length; i++)
            {
                Object key = this.getClass().getDeclaredField(keyColumn()[i]).get(this);
                if (key != null)
                {
                    usedKey.add(this.getClass().getSimpleName() + ":" + key);
                }
            }
            if (usedKey.size() == 0)
            {
                return;
            }
            Jedis jedis = iRedis.getJedis();
            String[] s = new String[0];
            jedis.del(usedKey.toArray(s));
            jedis.close();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    default void saveToRedis()
    {
        try
        {
            DataResult<String> usedKey = new DataResult<>();
            for (int i = 0; i < keyColumn().length; i++)
            {
                Object key = this.getClass().getDeclaredField(keyColumn()[i]).get(this);
                if (key != null)
                {
                    usedKey.add(key.toString());
                }
            }
            if (usedKey.size() == 0)
            {
                return;
            }
            Jedis jedis = iRedis.getJedis();
            usedKey.forEachForcely(key ->
            {
                Field[] fields = this.getClass().getDeclaredFields();
                String jedisKey = this.getClass().getSimpleName() + ":" + key;
                for (int i = 0; i < fields.length; i++)
                {
                    Field field = fields[i];
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
                    {
                        continue;
                    }
                    Object val = field.get(this);
                    if (val != null)
                    {
                        jedis.hset(jedisKey, field.getName(), val.toString());
                    }
                }
            });
            jedis.close();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
    }

    default boolean fillFromRedis()
    {
        try
        {
            String usedKey = "";
            for (int i = 0; i < keyColumn().length; i++)
            {
                Object key = this.getClass().getDeclaredField(keyColumn()[i]).get(this);
                if (key != null)
                {
                    usedKey = key.toString();
                    break;
                }
            }
            if (usedKey.isEmpty())
            {
                return false;
            }
            Field[] fields = this.getClass().getDeclaredFields();
            String jedisKey = this.getClass().getSimpleName() + ":" + usedKey;
            iApp.debug(this.getClass().getSimpleName()+".fillFromRedis",jedisKey);
            Jedis jedis = iRedis.getJedis();
            Map<String, String> map = jedis.hgetAll(jedisKey);
//            iApp.debug(iRedisModel.class.getSimpleName() + ".fillFromRedis", map.toString());
            if (map.size() == 0)
            {
                jedis.close();
                return false;
            }
            for (int i = 0; i < fields.length; i++)
            {
                Field field = fields[i];
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }
                String fieldName = field.getName();
                if (!map.containsKey(fieldName))
                {
                    continue;
                }
                String value = map.get(fieldName);
                if (field.getType().equals(Integer.class))
                {
                    field.set(this, Integer.parseInt(value));
                } else if (field.getType().equals(Long.class))
                {
                    field.set(this, Long.parseLong(value));
                } else if (field.getType().equals(Boolean.class))
                {
                    field.set(this, Boolean.parseBoolean(value));
                } else if (field.getType().isEnum())
                {
                    if (value.startsWith("\"") && value.endsWith("\""))
                    {
                        value = value.substring(1, value.length() - 1);
                    }
                    Enum a = Enum.valueOf((Class) (field.getType()), value);
                    field.set(this, a);
                } else
                {
                    field.set(this, value);
                }
            }
            jedis.close();
            return true;
        }
        catch (Exception e)
        {
            iApp.error(e);
        }
        return false;
    }
}
