package interpretation; 
import java.util.ArrayList;



public class Symbol {
	public int staffX, staffY;
	public ArrayList<int[]> noteHeads;
	public MusicImage myImage;
	public int staffDist;
	public String type;
	public boolean playing;
    public double certainty;
	
	public double[] horizontalBlackDistribution;
	public double[] verticalBlackDistribution;
	public int[] horizontalBlackSegments;
	public int[] horizontalWhiteSegments;
	public int[] verticalBlackSegments;
	public int[] verticalWhiteSegments;
	
	public Symbol(int x, int y, MusicImage img, int averageStaffDistance){
		staffX = x;
		staffY = y;
		myImage = img;
		staffDist = averageStaffDistance;
		playing = false;
		crop();
		
		noteHeads = new ArrayList<int[]>();
	}
	
	public double getRelativeHeight(){
		return myImage.getHeight()/(double)staffDist;
	}
	
	public double getRelativeWidth(){
		return myImage.getWidth()/(double)staffDist;
	}
	
	public double[] hDistribution(){
		double[] dist = new double[myImage.getHeight()];
		for(int y = 0; y < myImage.getHeight(); y++){
			for(int x = 0; x < myImage.getWidth(); x++){
				if(myImage.isBlack(x, y))
					dist[y]++;
			}
			dist[y] = (double)dist[y]/myImage.getHeight();
		}
		return dist;
	}
	
	//How much black is on each vertical line
	public double[] vDistribution(){
		double[] dist = new double[myImage.getWidth()];
		for(int x = 0; x < myImage.getWidth(); x++){
			for(int y = 0; y < myImage.getHeight(); y++){
				if(myImage.isBlack(x, y))
					dist[x]++;
			}
			dist[x] = (double)dist[x]/myImage.getHeight();
		}
		return dist;
	}
	
	
	public int[] hBlackSegments(){
		int[] segs = new int[myImage.getHeight()];
		for(int y = 0; y < myImage.getHeight(); y++){
			for(int x = 0; x < myImage.getWidth(); x++){
				if(segs[y]==0&&myImage.isBlack(x, y))
					segs[y] = 1;
				else if(myImage.isBlack(x,y)&&!myImage.isBlack(Math.max(0, x-1), y))
					segs[y]++;
			}
		}
		return segs;
	}
	
	public int[] hWhiteSegments(){
		int[] segs = new int[myImage.getHeight()];
		for(int y = 0; y < myImage.getHeight(); y++){
			for(int x = 0; x < myImage.getWidth(); x++){
				if(segs[y]==0&&!myImage.isBlack(x, y))
					segs[y] = 1;
				else if(!myImage.isBlack(x,y)&&myImage.isBlack(Math.max(0, x-1), y))
					segs[y]++;
			}
		}
		return segs;
	}
	
	public int[] vBlackSegments(){
		int[] segs = new int[myImage.getWidth()];
		for(int x = 0; x < myImage.getWidth(); x++){
			for(int y = 0; y < myImage.getHeight(); y++){
				if(segs[x]==0&&myImage.isBlack(x, y))
					segs[x] = 1;
				else if(myImage.isBlack(x,y)&&!myImage.isBlack(x, Math.max(0, y-1)))
					segs[x]++;
			}
		}
		return segs;
	}
	
	public int[] vWhiteSegments(){
		int[] segs = new int[myImage.getWidth()];
		for(int x = 0; x < myImage.getWidth(); x++){
			for(int y = 0; y < myImage.getHeight(); y++){
				if(segs[x]==0&&!myImage.isBlack(x, y))
					segs[x] = 1;
				else if(!myImage.isBlack(x,y)&&myImage.isBlack(x, Math.max(0, y-1)))
					segs[x]++;
			}
		}
		return segs;
	}
	
	public void setNoteHeads(){
		int[] noteHead = firstNoteHeadPast(0,0);
		if(myImage.getWidth()<staffDist||myImage.getHeight()<staffDist*2)
			return;
		int[] halfNoteHead = unfilledTopNoteHead();
		if(halfNoteHead==null)
			halfNoteHead = unfilledBottomNoteHead();
		if(halfNoteHead!=null){
			noteHeads.add(new int[]{halfNoteHead[0],halfNoteHead[1],0});
			noteHead=null;
		}
		while(noteHead!=null){
			noteHeads.add(noteHead);
			noteHead = firstNoteHeadPast(noteHead[0]+staffDist/2,0);
		}
		combineDuplicateNoteHeads();
	}
	
	public int[] firstNoteHeadPast(int x, int y){
		for(int searchX = x; searchX < myImage.getWidth(); searchX++){
			for(int searchY = y; searchY < myImage.getHeight(); searchY++){
				if(isFilledNoteHead(searchX, searchY))
					return new int[]{searchX,searchY,1};
			}
		}
		return null;
	}
	
	public boolean isFilledNoteHead(int x, int y){
		if(myImage.getHeight()<staffDist*3)
			return false;	
		if(x+staffDist<myImage.getWidth()&&myImage.isBlackInArea(x+staffDist,y-3,1,6))
			return false;
		if(y+(int)(staffDist/2.5)<myImage.getHeight()&&y>(int)(staffDist/2.5)&&x>staffDist/2&&x+staffDist/2<myImage.getWidth()){
			if(myImage.isAllBlackInArea((int)(x-staffDist/2.0), y, (int)(staffDist/1.0), 1)){
				if(myImage.isAllBlackInArea((int)(x-staffDist/2.5), y-staffDist/4, (int)(staffDist/2.5), staffDist/2)){
					if(myImage.isAllBlackInArea(x-staffDist/5, y-(int)(staffDist/2), staffDist/2, staffDist/2)){
										return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isUnfilledNoteHead(MusicImage possible){
		if(possible.percentBlackInArea(0, 0, possible.getWidth(), possible.getHeight())>70)
			return false;
		if(possible.percentBlackInArea(2, possible.getWidth()/4, possible.getWidth()/2, possible.getHeight()-2)>70)
			return false;
		if(!possible.isBlackInArea(0, 0, possible.getWidth()/2, possible.getHeight()/2))
			return false;
		if(!possible.isBlackInArea(0, possible.getHeight()/2+1, possible.getWidth()/2, possible.getHeight()/2-2))
			return false;
		if(!possible.isBlackInArea(possible.getWidth()/2+1, 0, possible.getWidth()/2-2, possible.getHeight()/2))
			return false;
		if(!possible.isBlackInArea(possible.getWidth()/2+1, possible.getHeight()/2+1, possible.getWidth()/2-2, possible.getHeight()/2-2))
			return false;
		return true;
	}
	
	public int[] unfilledBottomNoteHead(){
		if(myImage.getWidth()>staffDist*2)
			return null;
		if(myImage.isBlackInArea(0, 0, myImage.getWidth()/2, myImage.getHeight()-(int)(staffDist*1.5)))
			return null;
		if(myImage.percentBlackInArea(0, 0, myImage.getWidth(), myImage.getHeight()-(int)(staffDist*1.5))>30)
			return null;
		
		MusicImage possible1 = myImage.getSubMusicImage(0,myImage.getHeight()-(int)(staffDist*1.1),myImage.getWidth(), (int)(staffDist*1.1));
		if(!isUnfilledNoteHead(possible1))
			return null;
		
		return new int[]{possible1.getWidth()/2,myImage.getHeight()-possible1.getHeight()/2};
		
	}
	
public int[] unfilledTopNoteHead(){
		if(myImage.getWidth()>staffDist*2)
			return null;
		if(myImage.getHeight()<staffDist*2)
			return null;
		if(myImage.percentBlackInArea(myImage.getWidth()/2, myImage.getHeight()/2, myImage.getWidth()/2, myImage.getHeight()/2)>20)
			return null;
		if(myImage.percentBlackInArea(0, (int)(staffDist*1.5), myImage.getWidth(), myImage.getHeight()-(int)(staffDist*1.5))>30)
			return null;

		MusicImage possible1 = myImage.getSubMusicImage(0,0,myImage.getWidth(), (int)(staffDist*1.1));
		if(!isUnfilledNoteHead(possible1))
			return null;
		
		return new int[]{possible1.getWidth()/2,(int)(staffDist*.6)};
	}
	
	public void crop(){
		int xStart = -1,xEnd = -1,yStart = -1,yEnd = -1;
		for(int x = 0; x < myImage.getWidth(); x++){
			for(int y = 0; y < myImage.getHeight(); y++){
				if(myImage.isBlack(x,y)){
					xStart = x;
					break;
				}
			}
			if(xStart>=0)
				break;
		}
		for(int x = myImage.getWidth()-1; x >-1; x--){
			for(int y = 0; y < myImage.getHeight(); y++){
				if(myImage.isBlack(x,y)){
					xEnd = x;
					break;
				}
			}
			if(xEnd>=0)
				break;
		}
		
		for(int y = 0; y < myImage.getHeight(); y++){
			for(int x = 0; x < myImage.getWidth(); x++){
				if(myImage.isBlack(x,y)){
					yStart = y;
					break;
				}
			}
			if (yStart>-1)
				break;
		}
		for(int y = myImage.getHeight()-1; y > 0; y--){
			for(int x = 0; x < myImage.getWidth(); x++){
				if(myImage.isBlack(x,y)){
					yEnd = y;
					break;
				}
			}
			if (yEnd>-1)
				break;
		}
		if(yStart<0||xStart<0||xEnd-xStart+1<1||yEnd-yStart+1<1){
			return;
		}
		staffX = staffX + xStart;
		staffY = staffY + yStart;
		
		myImage = myImage.getSubMusicImage(xStart, yStart, xEnd-xStart+1, yEnd-yStart+1);
	}
	
	public void combineDuplicateNoteHeads(){
		for(int i = 1; i < noteHeads.size(); i++){
			if(noteHeads.get(i)[2]==noteHeads.get(i-1)[2]&&(Math.abs(noteHeads.get(i)[0]-noteHeads.get(i-1)[0])<staffDist&&Math.abs(noteHeads.get(i)[1]-noteHeads.get(i-1)[1])<staffDist)){
				int[] newNote = new int[]{(noteHeads.get(i)[0]+noteHeads.get(i-1)[0])/2,(noteHeads.get(i)[1]+noteHeads.get(i-1)[1])/2,noteHeads.get(i)[2]};
				noteHeads.set(i, newNote);
				noteHeads.remove(i-1);
				i--;
			}
		}
	}
	
	public int compareSegments(Symbol other){
		int compTotal = 0;
		
		if(verticalBlackSegments==null)
			verticalBlackSegments = vBlackSegments();
		if(other.verticalBlackSegments==null)
			other.verticalBlackSegments = other.vBlackSegments();
		
		if(verticalWhiteSegments==null)
			verticalWhiteSegments = vWhiteSegments();
		if(other.verticalWhiteSegments==null)
			other.verticalWhiteSegments = other.vWhiteSegments();
		
		if(horizontalWhiteSegments == null)
			horizontalWhiteSegments = hWhiteSegments();
		if(other.horizontalWhiteSegments == null)
			other.horizontalWhiteSegments = other.hWhiteSegments();
		
		if(horizontalBlackSegments == null)
			horizontalBlackSegments = hBlackSegments();
		if(other.horizontalBlackSegments == null)
			other.horizontalBlackSegments = other.hBlackSegments();
			
		for (int i = 0; i < 15; i++){
			int c = Math.abs(verticalBlackSegments[(int)(i*myImage.getWidth()/15.0)]-other.verticalBlackSegments[(int)(i*other.myImage.getWidth()/15.0)]);
			compTotal = compTotal + c; 
		}
		
		for (int i = 0; i < 15; i++){
			int c = Math.abs(horizontalBlackSegments[(int)(i*myImage.getHeight()/15.0)]-other.horizontalBlackSegments[(int)(i*other.myImage.getHeight()/15.0)]);
			compTotal = compTotal + c; 
		}
		
		return compTotal;
	}
	
	//Check the distribution values at 15 points
	public double compareDistribution(Symbol other){
		double compTotal = 0;
		
		if(horizontalBlackDistribution==null)
			horizontalBlackDistribution = hDistribution();
		if(verticalBlackDistribution==null)
			verticalBlackDistribution = vDistribution();
		
		if(other.horizontalBlackDistribution==null)
			other.horizontalBlackDistribution = other.hDistribution();
		if(other.verticalBlackDistribution==null)
			other.verticalBlackDistribution = other.vDistribution();
		
		for (int i = 0; i < 15; i++){
			double c = verticalBlackDistribution[(int)(i*myImage.getWidth()/15.0)]-other.verticalBlackDistribution[(int)(i*other.myImage.getWidth()/15.0)];
			compTotal = compTotal + Math.pow(c,2); 
		}
		
		for (int i = 0; i < 15; i++){
			double c = horizontalBlackDistribution[(int)(i*myImage.getHeight()/15.0)]-other.horizontalBlackDistribution[(int)(i*other.myImage.getHeight()/15.0)];
			compTotal = compTotal + Math.pow(c,2); 
		}
		
		return compTotal;
	}
	
	public ArrayList<Symbol> separateSymbol(){
		ArrayList<Symbol> separated = new ArrayList<Symbol>();
		int start = 0;
		int end = (noteHeads.get(1)[0]+noteHeads.get(0)[0])/2;
		int i = 0;
		for(int[] noteHead : noteHeads){
			MusicImage subImg = new MusicImage(myImage.getSubimage(start, 0, end-start, myImage.getHeight()));
			separated.add(new Symbol(staffX+start, staffY, subImg, staffDist));
			separated.get(i).noteHeads.add(new int[]{noteHead[0]-start, noteHead[1]-(separated.get(i).staffY-staffY), noteHead[2]});
			i++;
			start = end;
			if(i < noteHeads.size()-1)
				end = (noteHeads.get(1+i)[0]+noteHeads.get(i)[0])/2;
			else
				end = myImage.getWidth();
		}
		return separated;
	}
	
	public void eraseExtraLines(int staffTop, int staffBottom){
		int y1 = -1;
		int y2 = -1;
		int lineBottom = 0;
		if(myImage.getHeight()<(staffBottom-staffTop)/4)
                    return;
		for(int y = 0; y +staffY < staffTop&&y<myImage.getHeight();y++){
			
			if(myImage.isAllBlackInArea(1, y,myImage.getWidth()-2, 1)&&y1 == -1){
				y1 = y;
			}
			else if(y1>-1&&y2 == -1 &&((!myImage.isAllBlackInArea(0, y,myImage.getWidth(), 1))||((y==myImage.getHeight()-1)&&myImage.isAllBlackInArea(1, y,myImage.getWidth()-2, 1)))){
				y2 = y;
				lineBottom = y2;
				y = lineBottom+1;
				for(int x = 0; x < myImage.getWidth(); x++){
					if(!myImage.blackAboveOrBelow(x, y1-2, y2+2)){
						for(int eraseY = y1; eraseY <= y2; eraseY++){
							myImage.setRGB(x,eraseY,0xFFFFFF);
						}
					}
				}
				y2 = -1;
				y1 = -1;
			}
		}
		y2 = -1;
		y1 = -1;
		for(int y = staffBottom-staffY; y < myImage.getHeight();y++){
			if(myImage.isAllBlackInArea(2, y,myImage.getWidth()-4, 1)&&y1 == -1)
				y1 = y;
			else if(y1>-1&&y2 == -1 &&!myImage.isAllBlackInArea(1, y,myImage.getWidth()-2, 1)){
				y2 = y;
				lineBottom = y2;
				y = lineBottom+1;
				for(int x = 0; x < myImage.getWidth(); x++){
					if(!myImage.blackAboveOrBelow(x, y1-2, y2+2)){
						for(int eraseY = y1; eraseY <= y2; eraseY++){
							myImage.setRGB(x,eraseY,0xFFFFFF);
						}
					}
				}
				y2 = -1;
				y1 = -1;
			}
		}
		crop();
	}  
}
