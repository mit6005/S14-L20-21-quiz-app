package multiplayerquiz.common.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ModelTesting {

    @Test
    /**
     * Should be able to get the original value at the boarders and middle
     */
    public void BoardStateGet() {
        BoardState bs = new BoardState(1000, 10, 5);
    
        assertEquals(bs.getAvailablePoints(1, 1), 1000);
        assertEquals(bs.getAvailablePoints(0, 4), 1000);
        assertEquals(bs.getAvailablePoints(9, 0), 1000);
        assertEquals(bs.getAvailablePoints(9, 4), 1000);
    }
    
    @Test
    /**
     * If board is updated, updates should be available 
     * and only in the new new board
     */
    public void BoardStateSet() {
        BoardState bs = new BoardState(1000, 10, 5);
        BoardState bs2 = bs.withUpdate(1, 3, 100);
        BoardState bs3 = bs2.withUpdate(3, 4, -100);
    
        assertEquals(bs.getAvailablePoints(1, 3), 1000);
        assertEquals(bs.getAvailablePoints(3, 4), 1000);
        assertEquals(bs.getAvailablePoints(0, 4), 1000);
        assertEquals(bs.getAvailablePoints(9, 0), 1000);
        assertEquals(bs.getAvailablePoints(9, 4), 1000);
        
        assertEquals(bs2.getAvailablePoints(1, 3), 1100);
        assertEquals(bs2.getAvailablePoints(3, 4), 1000);
        assertEquals(bs2.getAvailablePoints(0, 4), 1000);
        assertEquals(bs2.getAvailablePoints(9, 0), 1000);
        assertEquals(bs2.getAvailablePoints(9, 4), 1000);
        
        assertEquals(bs3.getAvailablePoints(1, 3), 1100);
        assertEquals(bs3.getAvailablePoints(3, 4), 900);
        assertEquals(bs3.getAvailablePoints(0, 4), 1000);
        assertEquals(bs3.getAvailablePoints(9, 0), 1000);
        assertEquals(bs3.getAvailablePoints(9, 4), 1000);
    }
    
    @Test
    /**
     * BoardState --> String --> BoardState --> String should give 
     * back the same string twice.
     * Different board structure should have different strings.
     */
    public void BoardStateParsing() {
        BoardState bs = new BoardState(1000, 10, 5);
        bs = bs.withUpdate(1, 3, 100);
        bs = bs.withUpdate(3, 4, -100);
        BoardState bsx = bs.withUpdate(3, 4, -100);
         
        String bss = bs.toProtocolString();
        BoardState bs2 = BoardState.parse(bss);
        String bss2 = bs2.toProtocolString();
        assertEquals(bss, bss2);
        
        String bssx = bsx.toProtocolString();
        assertNotSame(bss, bssx);
    }
    

}
