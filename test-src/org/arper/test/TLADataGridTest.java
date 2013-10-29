package org.arper.test;

import org.arper.turtle.aux.TLADataGrid;

public class TLADataGridTest {
    public static void main(String[] args) throws Exception {
        TLADataGrid grid = TLADataGrid.createFromFile("org/arper/test/test.grid");
        System.out.println(grid);
        System.out.println(grid.getSquare(2, 2));
    }
}
