import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class rte_TCPClientTest {

	
	  @Test
	    public void testThis() throws Exception {
	        rte_TCPClient client = new rte_TCPClient();
	        int specialNum = client.testNum;	        
	        assertEquals(specialNum, 66);
	    }
}
