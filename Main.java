public class Main {
    public static void main(String[] args) {
        // Grid for tetris game
        TetrisSolver tetrisSolver = new TetrisSolver(6, 8);

        // Set up initial board state
        tetrisSolver.addPuzzleRow("### ##");
        tetrisSolver.addPuzzleRow("  # # ");
        tetrisSolver.addPuzzleRow("### ##");
        tetrisSolver.addPuzzleRow("# #  #");
        tetrisSolver.addPuzzleRow("     #");

        // tetrisSolver.addPuzzleRow(" *** ");
        // tetrisSolver.addPuzzleRow("  *  ");
        

        // Add puzzle pieces
        tetrisSolver.addPuzzlePiece( "##\n##\n", 1); // returns id 1 (square)
        tetrisSolver.addPuzzlePiece( "####\n",1); // returns id 2 (horizontal bar)
        tetrisSolver.addPuzzlePiece( "## \n ##\n", 1); // returns id 3 (Z shape)
        tetrisSolver.addPuzzlePiece( " ##\n## \n", 1); // returns id 4(S shape)
        tetrisSolver.addPuzzlePiece( " # \n###\n", 1); // returns id 5(T shape)
        tetrisSolver.addPuzzlePiece( "#  \n###\n", 1); // returns id 6 (L shape)
        tetrisSolver.addPuzzlePiece( "  #\n###\n", 1); // returns id 7 (inverted L shape)

        // Display the initial board state
        System.out.println("Initial board state:");
        System.out.println(tetrisSolver.showPuzzle());

        tetrisSolver.placePiece(1, 1); 
        System.out.println(tetrisSolver.showPuzzle());
        
        tetrisSolver.placePiece(3, 1);
        System.out.println(tetrisSolver.showPuzzle());

        tetrisSolver.placePiece(3, 1);

        // Display the board after placement
        System.out.println("Board State After Adding Pieces: \n");
        System.out.println(tetrisSolver.showPuzzle());
        
        // Display score and penalty
        System.out.println("Score: " + tetrisSolver.getScore());
        System.out.println("Penalty: " + tetrisSolver.getPenalty());
    }
}

