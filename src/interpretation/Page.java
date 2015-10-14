package interpretation; 
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents one page of monophonic sheet music.
 * @author Caitlin Scarberry
 * 
 * 
 */
public class Page {
	public MusicImage wholePage;
	public ArrayList<int[]> allStaffLines;
	public int averageStaffDist;
	public ArrayList<MeasureRow> measureRows;
	public SymbolDictionary myDictionary;
	/**
	 * 
	 * @param img This is the full page image.
	 * 			  Though cropping out the title
	 * 			  and unnecessary white space will
	 * 		      speed up interpretation, it is 
	 *            not necessary. 300-400 ppi is the
	 *            ideal quality. 
	 */
	public Page(BufferedImage img){
		wholePage = new MusicImage(img);
		allStaffLines = staffLineYCoords();
		measureRows = new ArrayList<MeasureRow>();
		
		try {
			myDictionary = new SymbolDictionary();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		int count = 0;
		for(int[] b : rowsOfMeasures()){
			int[][] thisMeasureLines = new int[5][];
			for(int i = count*5; i < count*5+5; i++){
				thisMeasureLines[i%5] = new int[]{allStaffLines.get(i)[0]-b[0],allStaffLines.get(i)[1]-b[0]};
			}
			count++;
			measureRows.add(new MeasureRow(wholePage, b[0], b[1], thisMeasureLines, myDictionary));
		}
	}
	
	/**
	 * 
	 * @return Returns an array of the top and bottom y coordinates of staff lines
	 */
		public ArrayList<int[]> staffLineYCoords(){
			ArrayList<int[]> lineCoords = new ArrayList<int[]>();
			int[] distribution = new int[wholePage.getHeight()];
			int maxBlack = 0;
			for(int y = 0; y < distribution.length; y++){
				int blackPx = 0;
				for(int x = 0; (x < wholePage.getWidth()&&blackPx>0)||(x<wholePage.getWidth()/4); x++){
					if(wholePage.isBlack(x, y))
						blackPx++;
				}
				if(maxBlack<blackPx)
					maxBlack = blackPx;
				distribution[y] = blackPx;
			}
			boolean withinLine = false;
			int i = 0;
			for(int val : distribution){
				if((val>maxBlack*3.0/6&&!withinLine)){
					withinLine = true;
					lineCoords.add(new int[2]);
					lineCoords.get(lineCoords.size()-1)[0] = i;
				}
				else if(val<maxBlack*3.0/6&&withinLine){
					withinLine = false;
					lineCoords.get(lineCoords.size()-1)[1] = i-1;
				}
				i++;	
			}
			return lineCoords;
		}
	
	/**
	 * 
	 * @return Returns a list of the boundaries 
	 * 		   of each measurer row to be created.
	 * 		   Each measure row goes from
	 *	       halfway below the staff above it
	 *		   to halfway above the staff below it.
	 *         
	 */
	public ArrayList<int[]> rowsOfMeasures(){
		
		int[] thisMeasure;
		ArrayList<int[]> measureRows = new ArrayList<int[]>();
		ArrayList<int[]> lineCoords = staffLineYCoords();
		int upperBoundary = (int) Math.max(0, lineCoords.get(0)[0]*3-lineCoords.get(4)[1]*1.75);
		int lowerBoundary = (int) Math.max(0, lineCoords.get(0)[0]*3-lineCoords.get(4)[1]*1.75);
		for(int i = 0; i < lineCoords.size()/5; i++){
			if((i+1)*5<lineCoords.size())
				lowerBoundary = (lineCoords.get((i+1)*5-1)[1]+lineCoords.get((i+1)*5)[1])/2;
			else
				lowerBoundary = Math.min(wholePage.getHeight(),lineCoords.get((i+1)*5-1)[1]+lineCoords.get(Math.max(0,(i+1)*5-1))[1]-lineCoords.get(Math.max(0,(i)*5-1))[1]);
			
			thisMeasure = new int[]{upperBoundary, lowerBoundary};
			upperBoundary = lowerBoundary;
			measureRows.add(thisMeasure);
		}
		return measureRows;
	}
	
	public int staffStartX(MeasureRow mRow){
		return mRow.staffStartX;
	}
	/**
	 * Gets all symbols in each measure 
	 * row and interprets them. Must be 
	 * run before accessing any symbols.
	 */
    public void buildPage(){
        try{
            for(MeasureRow mRow : measureRows){    	
                mRow.getAllSymbols();
                mRow.interpretSymbols();
                mRow.getKeySignature();
			}
            for(int i = 1; i < measureRows.size(); i++){
            	if(measureRows.get(i).clef.equals("Previous row"))
            		measureRows.get(i).clef = measureRows.get(i-1).clef;
            }
        }
        catch(Exception e){
        	e.printStackTrace();
        	System.exit(0);
        }		
	}
}