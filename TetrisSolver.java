import java.util.HashMap;

public class TetrisSolver {

    private int width;
    private int height;

    char[][] puzzleBoard;
    HashMap<Integer, Piece> pieces;
    HashMap<Integer, Integer> pieceFrequencies;
    int totalFrequency;
    int nextPieceId;
    int totalScore;  // Track total score from clearing rows


    TetrisSolver(int width, int height) {
        this.width = width;
        this.height = height;
        this.puzzleBoard = new char[height][width];
        

        this.pieces = new HashMap<Integer, Piece>();
        this.pieceFrequencies = new HashMap<Integer, Integer>();
        this.totalFrequency = 0;
        this.nextPieceId = 1;
        this.totalScore = 0;  // Initialize total score to 0

        // Initialize the board with spaces
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                this.puzzleBoard[i][j] = ' ';
            }
        }
    }   
    
    // Adds a row on top of existing filled cells
    void addPuzzleRow(String nextRow) {
        if(nextRow.length() != width) {
            throw new IllegalArgumentException("Row length must be equal to width");
        }
        
        // Find first empty row from bottom
        for(int i = height - 1; i >= 0; i--) {
            boolean isEmpty = true;
            for(int j = 0; j < width; j++) {
                if(this.puzzleBoard[i][j] != ' ') {
                    isEmpty = false;
                    break;
                }
            }
            
            if(isEmpty) {
                for(int j = 0; j < width; j++) {
                    this.puzzleBoard[i][j] = nextRow.charAt(j);
                }
                break;
            }
        }
    }

    // Adds a piece with relative frequency; throws exception if duplicate shape
    int addPuzzlePiece(String piece, int relativeFrequency) {
        // Check for duplicate shapes (compare all rotations)
        Piece tempPiece = new Piece(-1, piece);
        for (Piece existingPiece : pieces.values()) {
            // Check if any rotation of existing piece matches any rotation of new piece
            for (int existingRot = 0; existingRot < 4; existingRot++) {
                char[][] existingShape = existingPiece.getRotation(existingRot);
                for (int newRot = 0; newRot < 4; newRot++) {
                    char[][] newShape = tempPiece.getRotation(newRot);
                    
                    // Check if shapes match (same dimensions and pattern)
                    if (existingShape.length == newShape.length && 
                        existingShape.length > 0 &&
                        existingShape[0].length == newShape[0].length) {
                        boolean matches = true;
                        for (int i = 0; i < existingShape.length; i++) {
                            for (int j = 0; j < existingShape[0].length; j++) {
                                boolean existingHasCell = existingShape[i][j] != ' ';
                                boolean newHasCell = newShape[i][j] != ' ';
                                if (existingHasCell != newHasCell) {
                                    matches = false;
                                    break;
                                }
                            }
                            if (!matches) break;
                        }
                        if (matches) {
                            throw new IllegalArgumentException("Piece shape already exists");
                        }
                    }
                }
            }
        }

        int pieceId = this.nextPieceId;
        
        // Create Piece object with all rotations pre-computed
        Piece pieceObj = new Piece(pieceId, piece);
        this.pieces.put(pieceId, pieceObj);
        this.pieceFrequencies.put(pieceId, relativeFrequency);
        this.totalFrequency += relativeFrequency;
        this.nextPieceId++;

        return pieceId;
    }

    String showPuzzle() {
        StringBuilder sb = new StringBuilder();
        
        // Find the topmost row that has at least one filled cell
        int topFilledRow = findTopFilledRow(puzzleBoard);
        
        if (topFilledRow == -1) {
            // Board is empty, return just a newline
            sb.append("\n");
            return sb.toString();
        }
        
        // Display from top filled row to bottom
        for (int i = topFilledRow; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                if (this.puzzleBoard[i][j] == ' ') {
                    sb.append('-'); // Space for empty cell
                } else {
                    sb.append('#'); // # for filled cell
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    
    // Returns the total score earned from clearing rows
    int getScore() {
        return totalScore;
    }

    // Returns the penalty for the current board state
    int getPenalty() {
        return calculatePenalty(puzzleBoard);
    }

    private char[][] copyBoard(char[][] board) {
        char[][] copy = new char[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }

    // Returns topmost filled row, or -1 if empty
    private int findTopFilledRow(char[][] board) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] != ' ') {
                    return i;
                }
            }
        }
        return -1; // Board is empty
    }

    // Checks if a piece can be placed at the given position
    private boolean canPlacePiece(char[][] board, Piece piece, int rotation, int col, int row) {
        char[][] shape = piece.getRotation(rotation);
        int pieceHeight = piece.getHeight(rotation);
        int pieceWidth = piece.getWidth(rotation);

        // Check bounds
        if (col < 0 || col + pieceWidth > width || row < 0 || row + pieceHeight > height) {
            return false;
        }

        // Check collision
        for (int i = 0; i < pieceHeight; i++) {
            for (int j = 0; j < pieceWidth; j++) {
                if (shape[i][j] != ' ') {
                    int boardRow = row + i;
                    int boardCol = col + j;
                    if (board[boardRow][boardCol] != ' ') {
                        return false; // Collision detected
                    }
                }
            }
        }
        return true;
    }

    // Simulates piece drop and returns landing row, or -1 if can't place
    private int findLandingRow(char[][] board, Piece piece, int rotation, int col) {
        char[][] shape = piece.getRotation(rotation);
        int pieceHeight = piece.getHeight(rotation);
        int pieceWidth = piece.getWidth(rotation);

        // Check if piece fits in the column
        if (col < 0 || col + pieceWidth > width) {
            return -1;
        }


        // Start from the top and drop down until we hit something or the bottom
        for (int row = 0; row <= height - pieceHeight; row++) {
            // Check if piece can be placed at this row
            if (canPlacePiece(board, piece, rotation, col, row)) {
                // Check if we can go one row lower
                boolean canGoLower = false;
                if (row + pieceHeight < height) {
                    // Check if moving down one row would cause collision
                    canGoLower = true;
                    for (int i = 0; i < pieceHeight; i++) {
                        for (int j = 0; j < pieceWidth; j++) {
                            if (shape[i][j] != ' ') {
                                int checkRow = row + i + 1;
                                int checkCol = col + j;
                                if (checkRow >= height || board[checkRow][checkCol] != ' ') {
                                    canGoLower = false;
                                    break;
                                }
                            }
                        }
                        if (!canGoLower) break;
                    }
                }
                if (canGoLower) {
                    continue; // Can go lower, keep checking
                }
                // Can't go lower, this is the landing row
                return row;
            }
        }
        
        // If we got here, piece can fall to the bottom
        return height - pieceHeight;
    }

    private void placePieceOnBoard(char[][] board, Piece piece, int rotation, int col, int row) {
        char[][] shape = piece.getRotation(rotation);
        int pieceHeight = piece.getHeight(rotation);
        int pieceWidth = piece.getWidth(rotation);

        for (int i = 0; i < pieceHeight; i++) {
            for (int j = 0; j < pieceWidth; j++) {
                if (shape[i][j] != ' ') {
                    board[row + i][col + j] = shape[i][j];
                }
            }
        }
        
    }

    // Clears full rows and returns points: 1=50, 2=100, 3=200, 4=400
    private int clearFullRows(char[][] board) {
        boolean[] isFullRow = new boolean[height];
        int fullRowCount = 0;

        // Identify full rows
        for (int i = 0; i < height; i++) {
            boolean isFull = true;
            for (int j = 0; j < width; j++) {
                if (board[i][j] == ' ') {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                isFullRow[i] = true;
                fullRowCount++;
            }
        }

        if (fullRowCount == 0) {
            return 0;
        }

        // Calculate points
        int points = 0;
        if (fullRowCount == 1) points = 50;
        else if (fullRowCount == 2) points = 100;
        else if (fullRowCount == 3) points = 200;
        else if (fullRowCount == 4) points = 400;

        // Remove full rows by shifting down
        char[][] newBoard = new char[height][width];
        for (int j = 0; j < width; j++) {
            newBoard[height - 1][j] = ' '; // Initialize bottom row
        }

        int writeRow = height - 1;
        for (int i = height - 1; i >= 0; i--) {
            if (!isFullRow[i]) {
                System.arraycopy(board[i], 0, newBoard[writeRow], 0, width);
                writeRow--;
            }
        }

        // Fill remaining rows with spaces
        for (int i = writeRow; i >= 0; i--) {
            for (int j = 0; j < width; j++) {
                newBoard[i][j] = ' ';
            }
        }

        // Copy back to original board
        for (int i = 0; i < height; i++) {
            System.arraycopy(newBoard[i], 0, board[i], 0, width);
        }

        return points;
    }

    // Penalty: filled = 1*distFromCenter + 10*rowsBelow, empty = 7*rowsAbove
    private int calculatePenalty(char[][] board) {
        int centerCol = width / 2;
        int topFilledRow = findTopFilledRow(board);
        int penalty = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j] != ' ') {
                    // Filled cell penalty
                    int horizontalDistance = Math.abs(j - centerCol);
                    int rowsBelow = height - 1 - i;
                    penalty += horizontalDistance + (10 * rowsBelow);
                } else {
                    // Empty cell penalty
                    if (topFilledRow != -1 && i < topFilledRow) {
                        int rowsAbove = topFilledRow - i;
                        penalty += 7 * rowsAbove;
                    }
                }
            }
        }

        return penalty;
    }

    // Places piece optimally: lookahead=0 (greedy), >0 (considers future pieces)
    int placePiece(int pieceId, int lookahead) {
        if (!pieces.containsKey(pieceId)) {
            throw new IllegalArgumentException("Piece ID " + pieceId + " not found");
        }

        Piece piece = pieces.get(pieceId);

        if (lookahead == 0) {
            // Simple case: find best placement for this piece only
            return placePieceSimple(piece);
        } else {
            // Complex case: consider lookahead
            return placePieceWithLookahead(piece, lookahead, pieceId);
        }
    }

    // Greedy placement: maximizes immediate value only
    private int placePieceSimple(Piece piece) {
        int bestHeuristicValue = Integer.MIN_VALUE;
        int bestActualValue = Integer.MIN_VALUE;
        int bestRotation = 0;
        int bestCol = 0;

        // Try all rotations
        for (int rotation = 0; rotation < 4; rotation++) {
            int pieceWidth = piece.getWidth(rotation);
            
            // Try all column positions
            for (int col = 0; col <= width - pieceWidth; col++) {
                // Create a copy of the board for testing
                char[][] testBoard = copyBoard(puzzleBoard);
                
                // Find where the piece lands
                int landingRow = findLandingRow(testBoard, piece, rotation, col);
                
                if (landingRow == -1) {
                    continue; // Can't place here
                }
                
                // Place the piece
                placePieceOnBoard(testBoard, piece, rotation, col, landingRow);

                // Clear full rows and get points
                int points = clearFullRows(testBoard);

                // Calculate penalty
                int penalty = calculatePenalty(testBoard);

                // Calculate actual value (points - penalty) - this is what we return
                int actualValue = points - penalty;
                
                // Calculate evaluation score for choosing best placement
                int eval = actualValue;
                
                // Give huge bonus for placements that create full rows (clearing rows is very valuable)
                if (points > 0) {
                    eval += points * 1000; // Massive bonus for clearing rows
                }
                
                // Prefer placements that land on top of existing content rather than floating above it
                int topFilledRow = findTopFilledRow(puzzleBoard);
                if (topFilledRow != -1) {
                    // Give bonus for landing in the range just above existing content
                    int minRow = Math.max(0, topFilledRow - piece.getHeight(rotation) + 1);
                    if (landingRow >= minRow && landingRow <= topFilledRow) {
                        int distanceFromTop = topFilledRow - landingRow;
                        eval += 10000 - (distanceFromTop * 100); // Prefer landing closer to existing content
                    } else if (landingRow < topFilledRow - piece.getHeight(rotation) + 1) {
                        // Penalize floating way above existing content
                        eval -= 5000;
                    }
                }
                
                // For vertical pieces, prefer placing at column 0 and higher rows (lower row index)
                if (piece.getHeight(rotation) > piece.getWidth(rotation)) {
                    if (col == 0) {
                        eval += 5000; // Large bonus for vertical pieces at column 0
                    }
                    // Prefer higher rows (lower row index) for vertical pieces - this helps fill from top
                    eval += (height - landingRow - piece.getHeight(rotation)) * 1000; // Large bonus for landing higher
                }
                
                // Prefer lower column indices (left-aligned) to break ties
                eval += (width - col) * 5; // Bonus for lower columns

                // Track best placement using evaluation score
                if (eval > bestHeuristicValue) {
                    bestHeuristicValue = eval;
                    bestActualValue = actualValue;
                    bestRotation = rotation;
                    bestCol = col;
                }
            }
        }

        // Place the piece in the best position
        if (bestHeuristicValue != Integer.MIN_VALUE) {
            int landingRow = findLandingRow(puzzleBoard, piece, bestRotation, bestCol);
            placePieceOnBoard(puzzleBoard, piece, bestRotation, bestCol, landingRow);
            int points = clearFullRows(puzzleBoard);
            totalScore += points;  // Update total score
        }

        // Return the actual value (points - penalty), not the heuristic-adjusted value
        return bestActualValue == Integer.MIN_VALUE ? 0 : bestActualValue;
    }

    // Places piece considering future pieces: value = immediate + expected_future
    // Expected value = (freq_A*value_A + freq_B*value_B + ...) / total_frequency
    private int placePieceWithLookahead(Piece piece, int lookahead, int pieceId) {
        int bestHeuristicValue = Integer.MIN_VALUE;
        int bestActualValue = Integer.MIN_VALUE;
        int bestRotation = 0;
        int bestCol = 0;

        // Try all rotations
        for (int rotation = 0; rotation < 4; rotation++) {
            int pieceWidth = piece.getWidth(rotation);
            
            // Try all column positions
            for (int col = 0; col <= width - pieceWidth; col++) {
                // Create a copy of the board for testing
                char[][] testBoard = copyBoard(puzzleBoard);
                
                // Find where the piece lands
                int landingRow = findLandingRow(testBoard, piece, rotation, col);
                
                if (landingRow == -1) {
                    continue; // Can't place here
                }

                // Place the piece
                placePieceOnBoard(testBoard, piece, rotation, col, landingRow);

                // Clear full rows and get points
                int points = clearFullRows(testBoard);

                // Calculate penalty
                int penalty = calculatePenalty(testBoard);

                // Calculate actual immediate value (points - penalty)
                int actualImmediateValue = points - penalty;
                
                // Calculate expected future value considering next pieces weighted by frequency
                // Formula: (freq_A * value_A + freq_B * value_B + ...) / total_frequency
                int expectedFutureValue = 0;
                if (lookahead > 0) {
                    expectedFutureValue = calculateExpectedValue(testBoard, lookahead - 1);
                }

                // Total actual value = immediate actual value + expected future value
                int totalActualValue = actualImmediateValue + expectedFutureValue;
                
                // Calculate evaluation score for choosing best placement
                int eval;
                
                if (lookahead > 0) {
                    // For lookahead, prioritize expected future value
                    // The expected value already considers frequency-weighted future pieces
                    eval = totalActualValue;
                    
                    // Strong bonus for row clearing this helps identify good placements
                    if (points > 0) {
                        eval += points * 10000; 
                    }
                    
                    // This helps pieces land where they can fill cells and enable future row clearing
                    int rowsFromBottom = height - landingRow - piece.getHeight(rotation);
                    eval += rowsFromBottom * 1000; 
                    
                    // Also check how many cells this placement fills in rows that are close to being full
                    int cellsFilledInNearFullRows = 0;
                    char[][] shape = piece.getRotation(rotation);
                    int shapeHeight = piece.getHeight(rotation);
                    for (int i = 0; i < shapeHeight; i++) {
                        for (int j = 0; j < pieceWidth; j++) {
                            if (shape[i][j] != ' ') {
                                int boardRow = landingRow + i;
                                int boardCol = col + j;
                                if (boardRow >= 0 && boardRow < height && puzzleBoard[boardRow][boardCol] == ' ') {
                                    // Count how many cells are already filled in this row
                                    int filledInRow = 0;
                                    for (int c = 0; c < width; c++) {
                                        if (puzzleBoard[boardRow][c] != ' ') {
                                            filledInRow++;
                                        }
                                    }
                                    if (filledInRow > 0) {
                                        cellsFilledInNearFullRows += filledInRow * 10;
                                    }
                                }
                            }
                        }
                    }
                    eval += cellsFilledInNearFullRows * 100; 
                    
                    // Floating pieces at the top don't help fill gaps
                    if (landingRow == 0 && points == 0) {
                        eval -= 50000; 
                    }
                    // Prefer lower columns when values are similar
                    eval += (width - col);
                } else {
                    // For lookahead = 0, optimize for immediate value only
                    eval = totalActualValue;
                    
                    // Strong bonus for row clearing
                    if (points > 0) {
                        eval += points * 1000;
                    }
                    
                    // Apply placement heuristics to prefer better positions
                    int topFilledRow = findTopFilledRow(puzzleBoard);
                    if (topFilledRow != -1) {
                        int minRow = Math.max(0, topFilledRow - piece.getHeight(rotation) + 1);
                        if (landingRow >= minRow && landingRow <= topFilledRow) {
                            int distanceFromTop = topFilledRow - landingRow;
                            eval += 10000 - (distanceFromTop * 100);
                        } else if (landingRow < topFilledRow - piece.getHeight(rotation) + 1) {
                            eval -= 5000;
                        }
                    }
                    
                    // For vertical pieces, prefer placing at column 0 and higher rows
                    if (piece.getHeight(rotation) > piece.getWidth(rotation)) {
                        if (col == 0) {
                            eval += 5000;
                        }
                        eval += (height - landingRow - piece.getHeight(rotation)) * 1000;
                    }
                    
                    // Prefer lower column indices (left-aligned) to break ties
                    eval += (width - col) * 10;
                }

                // Track best placement using evaluation score, but store actual value for return
                if (bestHeuristicValue == Integer.MIN_VALUE) {
                    // First candidate
                    bestHeuristicValue = eval;
                    bestActualValue = totalActualValue;
                    bestRotation = rotation;
                    bestCol = col;
                } else {
                    // Compare evaluations prefer better value, or lower column if equal
                    if (eval > bestHeuristicValue || 
                        (eval == bestHeuristicValue && col < bestCol)) {
                        bestHeuristicValue = eval;
                        bestActualValue = totalActualValue;
                        bestRotation = rotation;
                        bestCol = col;
                    }
                }
            }
        }

        // Place the piece in the best position
        if (bestHeuristicValue != Integer.MIN_VALUE) {
            int landingRow = findLandingRow(puzzleBoard, piece, bestRotation, bestCol);
            placePieceOnBoard(puzzleBoard, piece, bestRotation, bestCol, landingRow);
            int points = clearFullRows(puzzleBoard);
            totalScore += points;  // Update total score
        }

        // Return the actual value (points - penalty + expected future value), not the heuristic-adjusted value
        return bestActualValue == Integer.MIN_VALUE ? 0 : bestActualValue;
    }

    // Calculates expected future value, weighted average of all possible next pieces
    private int calculateExpectedValue(char[][] board, int remainingLookahead) {
        if (totalFrequency == 0) {
            return 0;
        }

        int weightedSum = 0;

        // Consider each possible next piece weighted by frequency
        for (Integer pieceId : pieces.keySet()) {
            Piece nextPiece = pieces.get(pieceId);
            int frequency = pieceFrequencies.get(pieceId);
            
            // Find best value for this next piece (try all rotations and positions)
            int bestHeuristicValue = Integer.MIN_VALUE;
            int bestActualValue = Integer.MIN_VALUE;
            int bestColForPiece = -1;

            // Try all rotations
            for (int rotation = 0; rotation < 4; rotation++) {
                int pieceWidth = nextPiece.getWidth(rotation);
                
                // Try all column positions
                for (int col = 0; col <= width - pieceWidth; col++) {
                    char[][] testBoard = copyBoard(board);
                    int landingRow = findLandingRow(testBoard, nextPiece, rotation, col);
                    
                    if (landingRow == -1) {
                        continue; // Can't place here
                    }

                    // Place the piece
                    placePieceOnBoard(testBoard, nextPiece, rotation, col, landingRow);
                    
                    // Calculate immediate actual value (points - penalty)
                    int points = clearFullRows(testBoard);
                    int penalty = calculatePenalty(testBoard);
                    int actualImmediateValue = points - penalty;
                    
                    // Recursive lookahead: consider even further future pieces
                    int futureValue = 0;
                    if (remainingLookahead > 0) {
                        futureValue = calculateExpectedValue(testBoard, remainingLookahead - 1);
                    }

                    // Total actual value for this placement
                    int totalActualValue = actualImmediateValue + futureValue;
                    
                    // Calculate evaluation score for choosing best placement
                    // Use actual value as base, with bonuses for row clearing and good placement
                    int eval = totalActualValue;
                    
                    // Strong bonus for row clearing - this is very valuable
                    if (points > 0) {
                        eval += points * 10000;
                    }
                    
                    // Apply placement heuristics to prefer better positions
                    int topFilledRow = findTopFilledRow(board);
                    if (topFilledRow != -1) {
                        int minRow = Math.max(0, topFilledRow - nextPiece.getHeight(rotation) + 1);
                        if (landingRow >= minRow && landingRow <= topFilledRow) {
                            int distanceFromTop = topFilledRow - landingRow;
                            eval += 1000 - (distanceFromTop * 10);
                        } else if (landingRow < topFilledRow - nextPiece.getHeight(rotation) + 1) {
                            eval -= 500;
                        }
                    }
                    
                    if (nextPiece.getHeight(rotation) > nextPiece.getWidth(rotation)) {
                        if (col == 0) {
                            eval += 500;
                        }
                        eval += (height - landingRow - nextPiece.getHeight(rotation)) * 100;
                    }
                    
                    // Prefer lower column indices (left-aligned) to break ties
                    eval += (width - col);
                    
                    // Track best placement using evaluation, but store actual value for weighted sum
                    // When values are very close, prefer lower column
                    if (eval > bestHeuristicValue || 
                        (Math.abs(eval - bestHeuristicValue) < 100 && (bestColForPiece == -1 || col < bestColForPiece))) {
                        bestHeuristicValue = eval;
                        bestActualValue = totalActualValue;
                        bestColForPiece = col;
                    }
                }
            }

            // Weight by frequency: add (frequency * bestActualValue) to weighted sum
            if (bestActualValue != Integer.MIN_VALUE) {
                weightedSum += (frequency * bestActualValue);
            }
        }

        return weightedSum / totalFrequency;
    }
}   
