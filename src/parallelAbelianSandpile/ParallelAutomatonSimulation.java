//Copyright M.M.Kuttel 2024 CSC2002S, UCT
//Modified by TSHBON035 B.Tshwane 2024 CSC2002S, UCT
package parallelAbelianSandpile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
/** parallel program to simulate an Abelian Sandpile cellular automaton.
 */

class ParallelAutomatonSimulation {
	static final boolean DEBUG=false;  //for debugging output, off
	
	static long startTime = 0;
	static long endTime = 0;

	//timers - note milliseconds
	private static void tick(){  //start timing
		startTime = System.currentTimeMillis();
	}
	private static void tock(){  //end timing
		endTime=System.currentTimeMillis(); 
	}
	
	//input is via a CSV file
	 public static int [][] readArrayFromCSV(String filePath) {
		 int [][] array = null;
	        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	            String line = br.readLine();
	            if (line != null) {
	                String[] dimensions = line.split(",");
	                int width = Integer.parseInt(dimensions[0]);
	                int height = Integer.parseInt(dimensions[1]);
	               	System.out.printf("Rows: %d, Columns: %d\n", width, height);

	                array = new int[height][width];
	                int rowIndex = 0;

	                while ((line = br.readLine()) != null && rowIndex < height) {
	                    String[] values = line.split(",");
	                    for (int colIndex = 0; colIndex < width; colIndex++) {
	                        array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
	                    }
	                    rowIndex++;
	                }
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return array;
	    }
	 
    public static void main(String[] args) throws IOException  {

    	Grid simulationGrid;  //the cellular automaton grid
		ParallelGridUpdate parallelGrid;  //the cellular automaton grid updating
    	  	
    	//if (args.length!=2) {   //input is the name of the input and output files
    		//System.out.println("Incorrect number of command line arguments provided.");
    		//System.exit(0);
		//}

    	//Read argument values
  		String inputFileName = args[0];  //input file name
		String outputFileName= args[1];  // output file name
    
    	// Read from input .csv file
    	simulationGrid = new Grid(readArrayFromCSV(inputFileName));
   	
    	int counter=0;
    	tick();  //start timer
    	if(DEBUG) {
    		System.out.printf("starting config: %d \n",counter);
    		simulationGrid.printGrid();
    	}

		ForkJoinPool pool = ForkJoinPool.commonPool();
		boolean change;
		 do {
			 parallelGrid = new ParallelGridUpdate(simulationGrid, 1, simulationGrid.getRows() + 1);
			 pool.invoke(parallelGrid);  // Invoke the task to update the grid
			 change = false;  // Reset the change flag

			 // Check if any cell in the grid has changed
			 for( int i = 1; i<simulationGrid.getRows() + 1; i++ ) {
				 for (int j = 1; j < simulationGrid.getColumns() + 1; j++) {
					if (simulationGrid.grid[i][j]!=simulationGrid.updateGrid[i][j]) {
						change=true;  // If a change is detected, set the flag to true
						break;
					}
				}
				 if(change) break;  // Exit the outer loop if a change is detected
			 }  //end nested for

			 // If any changes were detected, move the grid to the next time step
			if (change) {
				simulationGrid.nextTimeStep();
				counter++;
			}
		 }
		 while (change);  // Continue the loop until no changes are detected
		tock();  //end timer
   		
        System.out.println("Simulation complete, writing image...");
    	simulationGrid.gridToImage(outputFileName);  // Write grid as an image

    	//simulation details
		System.out.printf("Number of steps to stable state: %d \n",counter);
		System.out.printf("Time: %d ms\n",endTime - startTime );  // Total computation time
    }
}