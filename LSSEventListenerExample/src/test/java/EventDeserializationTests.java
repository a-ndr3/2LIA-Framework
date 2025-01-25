import com.espertech.events.ComplexEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class EventDeserializationTests {
    private void putString(ByteBuffer buffer, String value, int size) {
        CharBuffer charBuffer = CharBuffer.wrap(String.format("%-" + size + "s", value).toCharArray());
        while (charBuffer.hasRemaining()) {
            buffer.putChar(charBuffer.get());
        }
    }

    @Test
    public void testComplexByteBufferDeserialization() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.putInt(1); // eventId

        putString(buffer, "SampleEventName", 20);
        putString(buffer, "TypeA", 10);

        buffer.putLong(System.currentTimeMillis()); // timestamp
        buffer.putDouble(123.45); // value
        buffer.putFloat(67.89f); // percentage
        buffer.put((byte) 1); // isActive
        buffer.putShort((short) 10); // level
        buffer.putChar('A'); // category
        buffer.put((byte) 5); // priority

        putString(buffer, "SourceSystem", 15); // source
        putString(buffer, "DestinationSystem", 15); // destination

        buffer.putLong(5000L); // duration
        buffer.putInt(1); // metadata size

        putString(buffer, "key1", 10); // metadata key
        putString(buffer, "value1", 20); // metadata value

        buffer.putInt(3); // dataPoints size
        buffer.putInt(100);
        buffer.putInt(200);
        buffer.putInt(300);

        buffer.flip();

        //var event = ComplexEvent.fromByteBuffer(buffer);

        //Assertions.assertEquals(1, event.eventId());
    }
}
