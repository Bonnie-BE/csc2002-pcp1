
package parallelAbelianSandpile;
import java.util.concurrent.RecursiveAction;

/** This class is for the grid updating using compute for the Abelian Sandpile cellular automaton,
 * it extends RecursiveAction to perform parallel computation on a grid using the ForkJoin framework.
 * @author Bonani Tshwane
 * @version 06 August 2024
 */

public class ParallelGridUpdate extends RecursiveAction {
    private Grid grid;
    private int startRow, endRow;
    private static final int SEQUENTIAL_CUTOFF = 105;  // Threshold for switching to sequential computation

    // Constructor for initializing the grid and the range of rows to process
    public ParallelGridUpdate(Grid grid, int startRow, int endRow) {
        this.grid = grid;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    // The compute method performs the main computation, either sequentially or by splitting tasks
    @Override
    public void compute() {
        //int SEQUENTIAL_CUTOFF = Math.max(1, grid.getRows()/(Runtime.getRuntime().availableProcessors()-1));
        int diff = endRow - startRow;

        // If the number of rows to process is less than or equal to the cutoff, process them sequentially
        if (diff <= SEQUENTIAL_CUTOFF) {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 1; j < grid.getColumns() + 1; j++) {
                    // Update the grid cell based on the current cell and its neighbours
                    grid.updateGrid[i][j] = (grid.grid[i][j] % 4) +
                            (grid.grid[i - 1][j] / 4) +
                            grid.grid[i + 1][j] / 4 +
                            grid.grid[i][j - 1] / 4 +
                            grid.grid[i][j + 1] / 4;
                }
            }
        }
        else {
            // If the number of rows is larger than the cutoff, split the task into two halves
            int middle = (startRow + endRow) / 2;

            // If the number of rows is larger than the cutoff, split the task into two halves
            ParallelGridUpdate left = new ParallelGridUpdate(grid, startRow, middle);
            ParallelGridUpdate right = new ParallelGridUpdate(grid, middle, endRow);

            // Fork the left task (start it asynchronously) and compute the right task immediately
            left.fork();
            right.compute();

            // Wait for the left task to finish
            left.join();
        }
    }
}
