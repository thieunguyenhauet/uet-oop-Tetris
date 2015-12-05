package oop.h4t.tetris;// Board.java
/**
 * Group 15: H4T
 * On 30/11/15.
 */


/**
 * Tao ban choi Tetris, cung cap cac kieu hinh.
 * Co chuc nang "undo".
 */
public class Board {
    private int width; // độ rộng board
    private int height; // độ cao board
    private boolean[][] grid; // mảng lưu tọa độ
    private boolean DEBUG = true;
    boolean committed;

    private boolean[][] prevGrid; // lưu lại grid

    /**
     * Tao ban choi trong voi su cho truoc do cao va rong cua tung block.
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new boolean[width][height];
        committed = true;
        prevGrid = new boolean[width][height];
    }

    /**
     * Tra ve do rong cua block.
     */
    public int getWidth() {
        return width;
    }


    /**
     * Tra ve do cao cua block.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Trả về giá trị max dộ cao của cột hiện tại. Nếu rỗng return 0
     */
    public int getMaxHeight() {
        int maxHeight = 0;
        for (int pos = 0; pos < width; pos++) {
            if (getColumnHeight(pos) > maxHeight) {
                maxHeight = getColumnHeight(pos);
            }
        }
        return maxHeight;
    }

    /**
     * Trả về giá trị y về các điểm dến của hình khi nó rơi thẳng theo trục x
     */
    public int dropHeight(Piece piece, int x) {
        int[] skirt = piece.getSkirt();
        int result = 0;
        int origY = getColumnHeight(x) + skirt[0];
        int maxStep = 0;
        int maxColHeight = getColumnHeight(x);
        for (int pos = 1; pos < skirt.length; pos++) {
            int step = skirt[0] - skirt[1];
            if (Math.abs(maxStep) < Math.abs(step)) {
                maxStep = step;
            }
            if (maxColHeight < getColumnHeight(x + pos)) {
                maxColHeight = getColumnHeight(x + pos);
            }
        }
        if (maxStep > 0) {
            result = origY - maxStep;
        } else if (maxStep < 0) {
            result = getColumnHeight(x);
        } else {
            result = maxColHeight;
        }
        return result;
    }

    /**
     * Trả về độ cao cột - giá trị tọa độ y block cao nhất +1. Trả về 0 nếu không có block nào
     */
    public int getColumnHeight(int x) {
        int maxColumn = 0;
        for (int pos = 0; pos < height; pos++) {
            if (grid[x][pos]) {
                maxColumn = 1 + pos;
            }
        }
        return maxColumn;
    }

    /**
     * Trả về số block trong dòng cho trước
     */
    public int getRowWidth(int y) {
        int count = 0;
        for (int pos = 0; pos < width; pos++) {
            if (grid[pos][y]) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Trả về true nếu block trong bảng. Các block bên ngoài luôn trả vè true
     */
    public boolean getGrid(int x, int y) {
        boolean result = false;
        try {
            result = grid[x][y];
        } catch (Exception e) {
            result = true;
        }
        return result;
    }

    public static final int PLACE_OK = 0;
    public static final int PLACE_ROW_FILLED = 1;
    public static final int PLACE_OUT_BOUNDS = 2;
    public static final int PLACE_BAD = 3;

    /**
     * Tao cac hinh cua tetris.
     * Cho phep tao cac chuc nang undo de quay lai 1 vi tri truoc do
     */
    public int place(Piece piece, int x, int y) {
        if (!committed) throw new RuntimeException("place commit problem");

        int result = PLACE_OK;
        committed = false;
        for (int pos = 0; pos < grid.length; pos++) {
            System.arraycopy(grid[pos], 0, prevGrid[pos], 0, grid[pos].length);
        }

        for (int pos = 0; pos < piece.getBody().length; pos++) {
            TPoint point = piece.getBody()[pos];
            int placeToPutAtX = point.x + x;
            int placeToPutAtY = point.y + y;
            if (x < 0 || y < 0 || placeToPutAtX > width - 1 || placeToPutAtY > height - 1) {
                return PLACE_OUT_BOUNDS;
            } else if (grid[placeToPutAtX][placeToPutAtY]) {
                return PLACE_BAD;
            } else {
                grid[placeToPutAtX][placeToPutAtY] = true;
                if (width == getRowWidth(placeToPutAtY)) {
                    result = PLACE_ROW_FILLED;
                }
            }
        }
        // Viet code vao day
        return result;
    }

    /**
     * Xoa dong khi no duoc lap day, va chuyen cac dong tren xuong.
     * Tra ve so cac dong con lai
     */
    public int clearRows() {
        int rowsCleared = 0;
        // YOUR CODE HERE
        int maxHeight = getMaxHeight();

        if (committed)
            for (int i = 0; i < grid.length; i++){
                System.arraycopy(grid[i], 0, prevGrid[i], 0, grid[i].length);
            }
        committed = false;
        for (int toIndex = 0; toIndex <= maxHeight; toIndex++) {
            if (width == getRowWidth(toIndex)) {
                // full
                int fromIndex = toIndex + 1;
                while (fromIndex <= maxHeight && width == getRowWidth(fromIndex))
                    fromIndex++;
                // copy grid
                for (int i = 0; i < width; i++) {
                    grid[i][toIndex] = grid[i][fromIndex];
                    grid[i][fromIndex] = false;
                }
                // đẩy hàng xuống
                int ceil = maxHeight-rowsCleared;
                while (fromIndex < ceil) {
                    for (int i=0; i < width; i++) {
                        grid[i][fromIndex] = grid[i][fromIndex+1];
                        grid[i][fromIndex+1] = false;
                    }
                    fromIndex++;
                }
                rowsCleared++;
            }
        }
        return rowsCleared;
    }


    /**
     * Khoi phuc lai trang thai truoc do 1 vi tri va 1 clearRows().
     * Undo chi duoc goi 1 lan (chi duoc quay lai vi tri truoc do 1 lan)
     */
    public void undo() {
        //Viet code tai day
        if(!committed){
            boolean[][] temp = grid;
            grid = prevGrid;
            prevGrid = temp;
            committed = true;
        }
    }


    /**
     * Ghi lai trang thai hien tai nhu la trang thai khoi tao mot lan choi moi.
     */
    public void commit() {
        committed = true;
        //Viet code tai day
    }


    /*
     Render trang thai cho nhu mot xau ky tu dang String. Su dung de in ra trang thai hien tai
    */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        for (int y = height - 1; y >= 0; y--) {
            buff.append('|');
            for (int x = 0; x < width; x++) {
                if (getGrid(x, y)) buff.append('+');
                else buff.append(' ');
            }
            buff.append("|\n");
        }
        for (int x = 0; x < width + 2; x++) buff.append('-');
        return (buff.toString());
    }
}
