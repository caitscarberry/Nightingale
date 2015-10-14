package interpretation; 
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;



public class MusicImage extends BufferedImage {

	public MusicImage(ColorModel cModel, WritableRaster raster, boolean alphaPremultiplied) {
		super(cModel, raster, alphaPremultiplied, null);
	}
	
	public MusicImage(int width, int height, int imageType) {
		super(width, height, TYPE_INT_RGB);
	}
	
	public MusicImage(BufferedImage img){
		super(img.getColorModel(), img.getRaster(), img.isAlphaPremultiplied(), null);
	}
	
	public int getDarkness(int x, int y){
		
		int rgb = getRGB(x,y);
		
		int red = (rgb & 0x00ff0000)/65536;
		int green = (rgb & 0x0000ff00)/256;
		int blue = rgb & 0x000000ff;
		int avg = (red+blue+green)/3;
		return avg;
	}
	
	public boolean isBlack(int x, int y){
		return getDarkness(x,y)<160;
	}
	
	public boolean isBlackInArea(int x1, int y1, int width, int height){
		while(x1<0){
			x1++;
			width--;
		}
		while(y1<0){
			y1++;
			height--;
		}
		for(int x = x1; (x < x1 + width)&&(x < getWidth()); x++){
			for(int y = y1; (y < y1 + height)&&(y < getHeight()); y++){
				if(isBlack(x,y))
					return true;
			}
		}
		return false;
	}
	
	public boolean isAllBlackInArea(int x1, int y1, int width, int height){
		if(x1<0||y1<0||y1 + height>getHeight()||x1+width > getWidth())
			return false;
		for(int x = x1; x < x1 + width; x++){
			for(int y = y1; y < (y1 + height); y++){
				if(!isBlack(x,y))
					return false;
			}
		}
		return true;
	}
	
	public boolean blackAboveOrBelow(int x, int y1, int y2){
		if (!isAllBlackInArea(x,y1-1, 1, 1)&&!isAllBlackInArea(x,y2+1, 1, 1)){
			return false;
		}
		return true;
	}
	
	public boolean isMostlyBlackInArea(int x1, int y1, int w, int h){
		int whitePixels = 0;
		while(x1<0){
			x1++;
			w--;
		}
		while(y1 < 0){
			y1++;
			h--;
		}
		for(int x = x1; x < x1+w && x<getWidth(); x++){
			for(int y = y1; y < y1 + h&&y<getHeight(); y++){
				if(!isBlack(x,y))
					whitePixels++;
			}
			if(whitePixels>w*h*.2)
				return false;
		}
		return true;
	}
	
	public MusicImage deepCopy(){
		return new MusicImage(getColorModel(), copyData(null), getColorModel().isAlphaPremultiplied());
	}
	
	public MusicImage getSubMusicImage(int x, int y, int w, int h){
		BufferedImage img = new BufferedImage(w,h,TYPE_INT_RGB);
		for(int copyX = x; (copyX < x+w)&&(copyX<getWidth()); copyX++){
			for(int copyY = y; (copyY < y+h)&&(copyY<getHeight()); copyY++){			
				img.setRGB(copyX-x, copyY-y, getRGB(copyX,copyY));
			}
		}
		return new MusicImage(img.getColorModel(), img.copyData(null), false);
	}
	public double percentBlackInArea(int x, int y, int w, int h){
		int black = 0;
		x = Math.max(0,x);
		y = Math.max(0,y);
		w = Math.min(getWidth()-x-1, w);
		h = Math.min(getHeight()-y-1, h);
		
		for(int xCheck = 0; xCheck<w; xCheck++){
			for(int yCheck = 0; yCheck<h; yCheck++){
				if(isBlack(xCheck+x, yCheck+y))
					black++;
			}
		}
		return (double)black/(w*h)*100;
	}

}
