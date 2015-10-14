package interpretation; 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.imageio.ImageIO;


public class SymbolDictionary {
	public HashMap<String, Symbol> dictionary;
	public Symbol bassClef;
	public Symbol altoClef;
	public SymbolDictionary() throws IOException{
		
		dictionary = new HashMap<String, Symbol>();
		MusicImage img = new MusicImage(ImageIO.read(new File("Dictionary/wholeNote.png")));
		dictionary.put("Whole note", new Symbol(0,0,img, (int)(img.getHeight()/1.1)));
		
		img = new MusicImage(ImageIO.read(new File("Dictionary/sharp.png")));
		dictionary.put("Sharp", new Symbol(0,0,img, (int)(img.getHeight()/2.5)));

		img = new MusicImage(ImageIO.read(new File("Dictionary/flat.png")));
		dictionary.put("Flat", new Symbol(0,0,img, (int)(img.getHeight()/2.4)));
	
		img = new MusicImage(ImageIO.read(new File("Dictionary/eighth rest.png")));
		dictionary.put("Eighth rest", new Symbol(0,0,img, (int)(img.getHeight()/1.9)));
	
        img = new MusicImage(ImageIO.read(new File("Dictionary/quarterRest.png")));
		dictionary.put("Quarter rest", new Symbol(0,0,img, (int)(img.getHeight()/3)));
                
		img = new MusicImage(ImageIO.read(new File("Dictionary/half or whole rest.png")));
		dictionary.put("half or whole rest", new Symbol(0,0,img, (int)(img.getHeight()*3/2.0)));
		
		img = new MusicImage(ImageIO.read(new File("Dictionary/dot.png")));
		dictionary.put("Dot", new Symbol(0,0,img, (int)(img.getHeight()*2.4)));
		
		img = new MusicImage(ImageIO.read(new File("Dictionary/natural.png")));
		dictionary.put("Natural", new Symbol(0,0,img, (int)(img.getHeight()/2.6)));
		
		img = new MusicImage(ImageIO.read(new File("Dictionary/bass clef.png")));
		bassClef = new Symbol(0,0,img, (int)(img.getHeight()/3.1));
	
		img = new MusicImage(ImageIO.read(new File("Dictionary/alto clef.png")));
		altoClef = new Symbol(0,0,img, (int)(img.getHeight()/4));
	}
	
	public String interpretClef(Symbol clef){
		if(clef.myImage.getHeight()/(double)clef.staffDist>5)
			return "Treble";
		else{
			if(clef.myImage.getHeight()/(double)clef.staffDist<3.8)
				return "Bass";
			else
				return "Alto";
		}
	}
	
	/**
	 * Evaluates the type of the symbol
	 * regardless of the symbol's placement
	 * on the staff. Adjustments due to 
	 * placement or nearby symbols is done
	 * by measureRow.interpretSymbols().
	 * @param s Symbol to be interpreted. 
	 * 		  Must not have a note head.
	 * @return The symbol's most likely type. 
	 */
	public String interpretSymbol(Symbol s){
		ArrayList<String> possibleMatches = new ArrayList<String>();
		for(String type : dictionary.keySet()){
			if(Math.abs(1-(s.myImage.getWidth()/(double)s.staffDist)/(dictionary.get(type).myImage.getWidth()/(double)dictionary.get(type).staffDist))<.2){
				if(Math.abs(1-(s.myImage.getHeight()/(double)s.staffDist)/(dictionary.get(type).myImage.getHeight()/(double)dictionary.get(type).staffDist))<.2){		
					possibleMatches.add(type);
				}
			}
		}	
		
		if(s.myImage.percentBlackInArea(0, s.myImage.getHeight()/2-2, s.myImage.getWidth()/5, 4)<70||s.myImage.percentBlackInArea(s.myImage.getWidth()/2, s.myImage.getHeight()/2, s.myImage.getWidth()/2, s.myImage.getHeight()/2)<40)
			possibleMatches.remove("Whole note");
			
		if(s.myImage.getHeight()/(double)s.staffDist>1.2||s.myImage.getWidth()/(double)s.staffDist>1.7)
			possibleMatches.remove("Whole note");

		if(possibleMatches.size()==0)
			return "None";
		
		else{
			HashMap<Integer, String> segmentMatches = new HashMap<Integer, String>();
			
			for(String type : possibleMatches){
				segmentMatches.put(dictionary.get(type).compareSegments(s),type);
			}
			
			while(segmentMatches.size()>2){
				segmentMatches.remove(Collections.max(segmentMatches.keySet()));
			}
			
			HashMap<Double, String> distributionMatches = new HashMap<Double, String>();
			for(String type : possibleMatches){
				distributionMatches.put(dictionary.get(type).compareDistribution(s),type);
			}
			
			while(distributionMatches.size()>3){
				distributionMatches.remove(Collections.max(distributionMatches.keySet()));
				
			}
			if(distributionMatches.containsValue(segmentMatches.get(Collections.min(segmentMatches.keySet())))&&Collections.min(segmentMatches.keySet())<15){
				
				return segmentMatches.get(Collections.min(segmentMatches.keySet()));
			}
            if(segmentMatches.containsValue(distributionMatches.get(Collections.min(distributionMatches.keySet())))&&Collections.min(segmentMatches.keySet())<15){
				return distributionMatches.get(Collections.min(distributionMatches.keySet()));
			}
				
			return "none";
		}
	}

	/**
	 * 
	 * @param s Symbol to be interpreted.
	 * @return "Eighth note", "Quarter note", "Sixteenth note", or "None". 
	 *
	 */
	public String interpretNonbeamedNote(Symbol s){
		MusicImage beams;
		if(s.noteHeads.get(0)[1]>s.myImage.getHeight()/2)
			beams = s.myImage.getSubMusicImage(0, 0, s.myImage.getWidth(), (int)(s.myImage.getHeight()/2-s.staffDist));
		else
			beams = s.myImage.getSubMusicImage(0, s.myImage.getHeight()/2+s.staffDist, s.myImage.getWidth(), s.myImage.getHeight()/2-s.staffDist);
		
		int maxSegments = 0;
		
		for(int x = 0; x < beams.getWidth()-8; x++){
			boolean onBlack = false;
			int segmentLength = 0;
			int segments = 0;
			
			for(int y = 0; y < beams.getHeight(); y++){
				if(beams.isBlack(x,y)&&beams.isBlack(x+1, y)&&beams.isBlack(x+2, y)&&beams.isBlack(x+3, y)&&beams.isBlack(x+4, y)&&beams.isBlack(x+5, y)&&beams.isBlack(x+6, y)&&beams.isBlack(x+7, y)&&beams.isBlack(x+8, y)){
					if(!onBlack){
						onBlack = true;
						segmentLength = 1;
					}
					else
						segmentLength++;
				}
				
				else if(segmentLength>1&&onBlack){
					segments++;	
					onBlack = false;
				}		
			}
			if(segments>maxSegments)
				maxSegments = segments;	
		}
		
		
		if(maxSegments == 0){
			if(s.myImage.getWidth()<s.staffDist*1.7)
				return "Quarter note";
			else
				return "Eighth note";
		}
		else if(maxSegments == 1)
			return "Eighth note";
		else if(maxSegments == 2)
			return "Sixteenth note";
		
		return "None";
	}
	
	/**
	 * 
	 * @param s Individual beamed note.
	 * @return "Eighth note", "Quarter note", "Sixteenth note", or "None".
	 */
	public String interpretBeamedNote(Symbol s){
		MusicImage beams;
		if(s.myImage.getHeight()<s.staffDist*2.5)
			return "None";
		if(s.noteHeads.get(0)[1]>s.myImage.getHeight()/2){
			beams = s.myImage.getSubMusicImage(0, 0, s.myImage.getWidth(), (int)(s.myImage.getHeight()/2-s.staffDist));
		}
		else{
			beams = s.myImage.getSubMusicImage(0, s.myImage.getHeight()/2, s.myImage.getWidth(), s.myImage.getHeight()/2);
		}
		int maxSegments = 0;
		ArrayList<Integer> allSegments = new ArrayList<Integer>();
		
		for(int x = 0; x < beams.getWidth(); x++){
			boolean onBlack = false;
			int segmentLength = 0;
			int segments = 0;
			
			for(int y = 0; y < beams.getHeight(); y++){
				if(beams.isBlack(x,y)){
					if(!onBlack){
						onBlack = true;
						segmentLength = 0;
					}
					else
						segmentLength++;
				}
				
				if(segmentLength>0&&onBlack&&(!beams.isBlack(x, y)||y==beams.getHeight()-1)){
					segments++;	
					onBlack = false;
				}	
			}
			allSegments.add(new Integer(segments));
			
		}
		
		if(Collections.frequency(allSegments, Collections.max(allSegments))>3)
				maxSegments = Collections.max(allSegments);
		else if (Collections.frequency(allSegments, Collections.max(allSegments)-1)>4)
			maxSegments = Collections.max(allSegments)-1;
			
		if(maxSegments < 2){
			return "Eighth note";
		}
		else if(maxSegments == 2){
			return "Sixteenth note";
		}
		
		else if(maxSegments == 0){
			return "None";
		}
		return "None";
	}
	
	public boolean isSplitWholeNote(Symbol sym1, Symbol sym2, Symbol newSym){
		if(Math.abs((double)sym1.myImage.getWidth()/sym2.myImage.getWidth()-1)>.2)
			return false;
		if(Math.abs((double)sym1.myImage.getHeight()/sym2.myImage.getHeight()-1)>.1)
			return false;
		if(Math.abs((double)sym1.myImage.getHeight()/(double)sym1.staffDist-1)>.1)
			return false;
		return newSym.compareDistribution(dictionary.get("Whole note"))<3;
	}
	
	public boolean isSplitFlat(Symbol left, Symbol right, Symbol whole){
		if((double)left.myImage.getHeight()/right.myImage.getHeight()<1.7)
			return false;
		if(left.myImage.getHeight()>left.staffDist*3)
			return false;
		if(right.staffY<left.staffY)
			return false;
		if(left.myImage.getWidth()>right.myImage.getWidth())
			return false;
		if(whole.myImage.getHeight()>whole.staffDist*2.5)
			return false;
		if(whole.myImage.getWidth()>whole.staffDist*1.5)
			return false;
		if(whole.compareDistribution(dictionary.get("Flat"))<3)
			return true;
		else
			return false;
	}
	
	public boolean isSplitHalfNote(Symbol left, Symbol right, Symbol whole){
		if(left.myImage.getHeight()<left.staffDist*3&&right.myImage.getHeight()<left.staffDist*3)
			return false;
		if(whole.myImage.getHeight()<whole.staffDist*2||whole.myImage.getHeight()>whole.staffDist*9)
			return false;
		if(whole.myImage.getWidth()>whole.staffDist*2||whole.myImage.getWidth()<whole.staffDist*.5)
			return false;
		whole.setNoteHeads();
		whole.combineDuplicateNoteHeads();
		if(whole.noteHeads.size()>0)
			return true;
		return false;
	}
	
	public String interpretReconstructedSymbol(Symbol part1, Symbol part2, Symbol whole){
		if(isSplitWholeNote(part1, part2, whole))
			return "Whole note";
		if(isSplitFlat(part1, part2, whole))
			return "Flat";
		if(isSplitHalfNote(part1, part2, whole))
			return "Half note";
		return "None";
	}
	
	public boolean isTie(Symbol s){
		if(s.horizontalBlackDistribution==null)
			s.horizontalBlackDistribution = s.hDistribution();
		if(s.verticalBlackDistribution==null)
			s.verticalBlackDistribution = s.vDistribution();

		if(s.horizontalBlackSegments == null)
			s.horizontalBlackSegments = s.hBlackSegments();
		if(s.verticalBlackSegments==null)
			s.verticalBlackSegments = s.vBlackSegments();
		
		for(int i : s.horizontalBlackSegments)
			if(i > 2)
				return false;
		for(int i : s.verticalBlackSegments)
			if(i > 2)
				return false;
		if(s.myImage.getWidth()<s.staffDist)
			return false;
		if(s.horizontalBlackSegments[s.horizontalBlackSegments.length/2]<s.horizontalBlackSegments[s.horizontalBlackSegments.length-1])
			return false;
		if(s.horizontalBlackSegments[s.horizontalBlackSegments.length/2]<s.horizontalBlackSegments[0])
			return false;
		return true;	
	}
}
