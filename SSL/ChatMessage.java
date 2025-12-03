package SSL;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Represents a chat message in the custom protocol.
 * Uses hybrid approach: binary header + JSON body for efficient transmission.
 * Supports serialization and deserialization for network communication.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class ChatMessage {
    private MessageType messageType;
    private int protocolVersion;
    private long timestamp;
    private String sender;
    private String recipient;
    private String content;
    private String roomId;
    
    /**
     * Constructs a ChatMessage with the specified type, sender and content.
     * Sets protocol version to 1 and timestamp to current time.
     * 
     * @param messageType the type of message (LOGIN_REQUEST, TEXT_MESSAGE, etc.)
     * @param sender the username of the sender
     * @param content the message content
     */
    public ChatMessage(MessageType messageType, String sender, String content) {
        this.messageType = messageType;
        this.protocolVersion = 1;
        this.timestamp = System.currentTimeMillis();
        this.sender = sender;
        this.content = content;
    }
    
    /**
     * Serializes the message to a byte array for network transmission.
     * Format: Binary header (10 bytes) + JSON body (variable size).
     * Header contains: version(1) + type(1) + length(4) + timestamp(4).
     * 
     * @return byte array representation of the message
     */
    public byte[] serialize() {
        // Create JSON body
        String json = "{\"type\":\"" + messageType + "\",\"sender\":\"" + sender + 
                      "\",\"content\":\"" + content + "\"}";
        byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);
        
        // Create binary header (10 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(10 + bodyBytes.length);
        buffer.put((byte) protocolVersion);              // 1 byte: protocol version
        buffer.put((byte) messageType.ordinal());        // 1 byte: message type
        buffer.putInt(bodyBytes.length);                 // 4 bytes: body length
        buffer.putInt((int) (timestamp / 1000));         // 4 bytes: timestamp
        buffer.put(bodyBytes);                           // variable: JSON body
        
        return buffer.array();
    }
    
    /**
     * Deserializes a byte array into a ChatMessage object.
     * Parses the binary header to extract metadata and decodes the JSON body.
     * 
     * @param data the byte array containing the serialized message
     * @return the reconstructed ChatMessage object
     */
    public static ChatMessage deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        
        // Parse binary header
        int version = buffer.get();
        int typeOrdinal = buffer.get();
        int bodyLength = buffer.getInt();
        long timestamp = buffer.getInt() * 1000L;
        
        // Parse JSON body
        byte[] bodyBytes = new byte[bodyLength];
        buffer.get(bodyBytes);
        String json = new String(bodyBytes, StandardCharsets.UTF_8);
        
        // Extract fields from JSON
        String sender = extractField(json, "sender");
        String content = extractField(json, "content");
        
        // Reconstruct message
        ChatMessage msg = new ChatMessage(MessageType.values()[typeOrdinal], sender, content);
        msg.setTimestamp(timestamp);
        
        return msg;
    }
    
    /**
     * Extracts a field value from a JSON string.
     * Simple JSON parsing for key-value pairs.
     * 
     * @param json the JSON string to parse
     * @param field the field name to extract
     * @return the field value, or empty string if not found
     */
    private static String extractField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    
    /**
     * Validates the message integrity and required fields.
     * Checks for null values and content length constraints.
     * 
     * @return true if the message is valid, false otherwise
     */
    public boolean validate() {
        return messageType != null && sender != null && content != null && content.length() <= 1000;
    }
    
    // Getters and Setters
    
    /**
     * @return the message type
     */
    public MessageType getMessageType() {
        return messageType;
    }
    
    /**
     * @return the sender username
     */
    public String getSender() {
        return sender;
    }
    
    /**
     * @return the message content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp for this message.
     * 
     * @param timestamp the timestamp in milliseconds
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Sets the recipient for private messages.
     * 
     * @param recipient the recipient username
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    /**
     * Sets the room identifier for room-based messages.
     * 
     * @param roomId the room identifier
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    /**
     * @return a string representation of the message
     */
    @Override
    public String toString() {
        return "ChatMessage{type=" + messageType + ", sender='" + sender + 
               "', content='" + content + "', timestamp=" + timestamp + "}";
    }
}
