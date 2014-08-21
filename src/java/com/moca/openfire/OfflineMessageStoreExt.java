package com.moca.openfire;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.openfire.OfflineMessageStore;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import redis.clients.jedis.Jedis;

/**
 * OfflineMessageStoreExt
 * @Description 解决MySQL链接负担，增加Redis缓存层，继承OfflineMessageStore重写离线策略，将离线信息保存到Redis中。
 * @author JX
 */
public class OfflineMessageStoreExt extends OfflineMessageStore {
	
	private static final Logger Log = LoggerFactory.getLogger(OfflineMessageStoreExt.class);
	
	private static OfflineMessageStoreExt instance = null;
	
	public static OfflineMessageStoreExt getInstance() {
		if(instance == null)
			instance = new OfflineMessageStoreExt();
        return instance;
    }
	
	/**
	 * 修改离线消息缓存策略，利用Redis List数据格式存储key = username； value = messageKey.
	 * @param username
	 * @param messageKey
	 * @param message
	 */
	public void addMessageKey(String username, String messageKey, Message message) {
		if (message == null) {
            return;
        }
        if (message.getBody() == null || message.getBody().length() == 0) {
        	if (message.getChildElement("event", "http://jabber.org/protocol/pubsub#event") == null)
        	{ 
        		return; 
        	}
        }
        JID recipient = message.getTo();
        if (username == null || !UserManager.getInstance().isRegisteredUser(recipient)) {
            return;
        }
        else
        if (!XMPPServer.getInstance().getServerInfo().getXMPPDomain().equals(recipient.getDomain())) {
            return;
        }
        
        Jedis jedis = null;
        try {
        	jedis = RedisClient.getInstance().jedis;
        	jedis.lpush(username, messageKey);
        }

        catch (Exception e) {
            Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
        }
        finally {
        	RedisClient.getInstance().destroy();
        }

	}
	
	/**
	 * 修改离线消息缓存策略，存储Value时使用String数据格式，key = messageId； value = message.
	 * @param messageId
	 * @param message
	 */
	public void addMessageValue(String messageId, Message message) {
		if (message == null) {
            return;
        }
        if (message.getBody() == null || message.getBody().length() == 0) {
        	if (message.getChildElement("event", "http://jabber.org/protocol/pubsub#event") == null)
        	{ 
        		return; 
        	}
        }
        JID recipient = message.getTo();
        String username = recipient.getNode();
        if (username == null || !UserManager.getInstance().isRegisteredUser(recipient)) {
            return;
        }
        else
        if (!XMPPServer.getInstance().getServerInfo().getXMPPDomain().equals(recipient.getDomain())) {
            return;
        }
        
        Jedis jedis = null;

        try {
        	//添加离线Delay消息时间
        	message.addChildElement("delay","urn:xmpp:delay")
        	.addAttribute("from", XMPPServer.getInstance().getServerInfo().getXMPPDomain())
        	.addAttribute("stamp", StringUtils.dateToMillis(new java.util.Date()));
        	
        	jedis = RedisClient.getInstance().jedis;
        	jedis.set(messageId, message.toXML());
        }

        catch (Exception e) {
            Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
        }
        finally {
        	RedisClient.getInstance().destroy();
        }

	}
	
	/**
	 * 返回该用户名下的所有消息ID
	 * @param username
	 * @return messageId
	 */
	public List<String> getMessageKeyList(String username) {
        List<String> msgKeyList = new ArrayList<String>();
        Jedis jedis = null;
        try {
            jedis = RedisClient.getInstance().jedis;
            msgKeyList = jedis.lrange(username, 0, -1);
        }
        catch (Exception e) {
            Log.error("Error retrieving offline messages of username: " + username, e);
        }
        finally {
        	RedisClient.getInstance().destroy();
        }
        return msgKeyList;
    }
	
	public void deleteMessage(String messageKey) {
		Jedis jedis = null;
        try {
        	jedis = RedisClient.getInstance().jedis;
        	jedis.del(messageKey);
        }
        catch (Exception e) {
            Log.error("Error deleting offline message of messageKey: " + messageKey, e);
        }
        finally {
        	RedisClient.getInstance().destroy();
        }
    }

}
