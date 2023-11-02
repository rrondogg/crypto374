import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class rte_TCPServerTest {
	
	@Test
	public void testThis() throws Exception {
        rte_TCPServer server = new rte_TCPServer();
        int specialNum = server.testNum;	        
        assertEquals(specialNum, 6);
    }
}
