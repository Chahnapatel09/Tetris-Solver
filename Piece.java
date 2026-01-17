public class Piece {
    int pieceId;
    char[][] baseShape;
    char[][][] rotations; // All 4 rotations: 0°, 90°, 180°, 270°
    int baseWidth;
    int baseHeight;

    Piece(int pieceId, String pieceString) {
        this.pieceId = pieceId;
        this.baseShape = parsePieceString(pieceString);
        this.baseHeight = baseShape.length;
        this.baseWidth = baseShape.length > 0 ? baseShape[0].length : 0;
        
        // Pre-compute all 4 rotations
        this.rotations = new char[4][][];
        this.rotations[0] = baseShape;
        this.rotations[1] = rotate90(baseShape);//90
        this.rotations[2] = rotate90(rotations[1]);//180
        this.rotations[3] = rotate90(rotations[2]);//270
    }

    private char[][] parsePieceString(String pieceString) {
        String[] lines = pieceString.split("\n");
        
        // Find the actual dimensions (trim empty lines)
        int maxWidth = 0;
        int nonEmptyLines = 0;
        
        for (String line : lines) {
            if (line.trim().length() > 0) {
                nonEmptyLines++;
                maxWidth = Math.max(maxWidth, line.length());
            }
        }
        
        if (nonEmptyLines == 0 || maxWidth == 0) {
            return new char[0][0];
        }
        
        char[][] shape = new char[nonEmptyLines][maxWidth];
        int row = 0;
        
        for (String line : lines) {
            if (line.trim().length() > 0) {
                for (int col = 0; col < maxWidth; col++) {
                    if (col < line.length()) {
                        shape[row][col] = line.charAt(col);
                    } else {
                        shape[row][col] = ' ';
                    }
                }
                row++;
            }
        }
        
        return shape;
    }

    // Rotates shape 90° clockwise
    private char[][] rotate90(char[][] shape) {
        if (shape.length == 0 || shape[0].length == 0) {
            return new char[0][0];
        }
        
        int originalHeight = shape.length;
        int originalWidth = shape[0].length;
        char[][] rotated = new char[originalWidth][originalHeight];
        
        for (int i = 0; i < originalHeight; i++) {
            for (int j = 0; j < originalWidth; j++) {
                rotated[j][originalHeight - 1 - i] = shape[i][j];
            }
        }
        
        return rotated;
    }

    char[][] getRotation(int rotation) {
        if (rotation < 0 || rotation >= 4) {
            throw new IllegalArgumentException("Rotation must be 0, 1, 2, or 3");
        }
        return rotations[rotation];
    }

    int getWidth(int rotation) {
        char[][] shape = getRotation(rotation);
        return shape.length > 0 ? shape[0].length : 0;
    }

    int getHeight(int rotation) {
        return getRotation(rotation).length;
    }
}

