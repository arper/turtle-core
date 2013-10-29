package org.arper.tlaux;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TLADataGrid {

    public static TLADataGrid createEmpty(int numRows, int numCols) {
        return new TLADataGrid(numRows, numCols);
    }

    public static TLADataGrid createFromFile(String fileName) throws IOException, TLAMalformedDataGridException {
        InputStream stream = null;
        try {
            URL resourceURL = ClassLoader.getSystemResource(fileName);
            if (resourceURL != null) {
                stream = resourceURL.openStream();
            } else {
                stream = new FileInputStream(fileName);
            }
            return createFromStream(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static TLADataGrid createFromStream(InputStream stream) throws TLAMalformedDataGridException {
        Scanner scanner = new Scanner(stream);
        List<String> lines = Lists.newArrayList();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (Strings.isNullOrEmpty(line)) {
                break;
            }

            lines.add(line);
        }

        return createFromLines(lines);
    }

    public static TLADataGrid createFromLines(Iterable<String> lines) throws TLAMalformedDataGridException {
        Preconditions.checkNotNull(lines, "lines cannot be null");

        if (Iterables.isEmpty(lines)) {
            return new TLADataGrid(0, 0);
        }

        int numRows = Iterables.size(lines);
        int numCols = lines.iterator().next().length();

        TLADataGrid data = new TLADataGrid(numRows, numCols);
        int row = 0;
        for (String line : lines) {
            if (line.length() != numCols) {
                throw new TLAMalformedDataGridException("Line " + (row + 1) + " has length "
                        + line.length() + " while previous lines have length " + numCols);
            }
            System.arraycopy(line.toCharArray(), 0, data.grid[row++], 0, numCols);
        }

        return data;
    }


    private TLADataGrid(int numRows, int numColumns) {
        this.grid = new char[numRows][numColumns];
    }

    private final char[][] grid;

    public int getNumRows() {
        return grid.length;
    }

    public int getNumColumns() {
        return grid[0].length;
    }

    public boolean isValidSquare(int row, int column) {
        return (row >= 0 && column >= 0) &&
                (row < getNumRows() && column < getNumColumns());
    }

    public Square getSquare(int row, int column) {
        return new Square(row, column);
    }

    public Square findSquareContaining(char value) {
        for (int row = 0; row < getNumRows(); row++) {
            for (int col = 0; col < getNumColumns(); col++) {
                if (grid[row][col] == value) {
                    return getSquare(row, col);
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("numRows", getNumRows())
                .add("numCols", getNumColumns())
                .add("data", Arrays.deepToString(grid))
                .toString();
    }

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();

        /* top column labels */
        sb.append(" c");
        for (int c = 0; c < getNumColumns(); c++) {
            sb.append(Integer.toString(c % 10));
        }
        sb.append('\n');

        /* top border */
        sb.append('r')
            .append('\u2554')
            .append(Strings.repeat("\u2550", getNumColumns()))
            .append('\u2557')
            .append('\n');

        /* rows with left/right labels */
        for (int r = 0; r < getNumRows(); r++) {
            sb.append(Integer.toString(r % 10))
                .append('\u2551')
                .append(grid[r])
                .append('\u2551')
                .append('\n');
        }

        sb.append(' ')
            .append('\u255A')
            .append(Strings.repeat("\u2550", getNumColumns()))
            .append('\u255D');

        return sb.toString();
    }

    public class Square {
        private Square(int row, int column) {
            this.row = row;
            this.column = column;
        }

        private final int row;
        private final int column;

        public boolean exists() {
            return isValidSquare(row, column);
        }

        private void ensureExists() {
            if (!exists()) {
                throw new IndexOutOfBoundsException("Square does not exist!");
            }
        }

        public char getValue() {
            ensureExists();
            return grid[row][column];
        }

        public void setValue(char value) {
            if (!exists()) {
                throw new IndexOutOfBoundsException("Square does not exist!");
            }
            grid[row][column] = value;
        }

        public Square translate(int deltaRow, int deltaColumn) {
            return new Square(row + deltaRow, column + deltaColumn);
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("row", row)
                    .add("column", column)
                    .add("exists", exists())
                    .add("value", exists()? ("'" + getValue() + "'") : null)
                    .toString();
        }
    }
}
