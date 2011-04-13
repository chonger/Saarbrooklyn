package saarbrooklyn;

import pointclick.SceneJump;
import processing.core.PImage;
import gifAnimation.Gif;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;

public class Player {
	
	Saarbrooklyn sb;
	Body bod;
	Vector2f dest;
	String name;
	Gif[] walk;
	PImage[] stand;
	int dir = 0;
	NonPlayer destNPC;
	String savedAction;
	boolean talk = false;
	String words = "";
	boolean cutControl = false;
	SceneJump jump = null;
	
	public Player(Saarbrooklyn sb, float x, float y, String name) {
		this.sb = sb;
		bod = new Body(new Circle(20),10);
		
		bod.setPosition(x, y);
		dest = null;
		this.name = name;
		walk = new Gif[4];
		walk[0] = new Gif(sb,name + "/" + name + "_walk_north.gif");
		walk[1] = new Gif(sb,name + "/" + name + "_walk_south.gif");
		walk[2] = new Gif(sb,name + "/" + name + "_walk_east.gif");
		walk[3] = new Gif(sb,name + "/" + name + "_walk_west.gif");
		for(int i=0;i<4;++i)
			walk[i].loop();
		
		stand = new PImage[4];
		stand[0] = sb.loadImage(name + "/" + name + "_face_north.gif");
		stand[1] = sb.loadImage(name + "/" + name + "_face_south.gif");
		stand[2] = sb.loadImage(name + "/" + name + "_face_east.gif");
		stand[3] = sb.loadImage(name + "/" + name + "_face_west.gif");
	}
	
	public void setDestination(Vector2f dest) {
		//sb.println("New dest = " + dest.toString());
		this.dest = dest;
	}
	
	public void update() {
		if(!cutControl) { //if the controls are cut, it means we just arrived and so we dont want to scenejump
			SceneJump[] sj = sb.scene.sceneJumps();
			
			for(SceneJump s : sj) {
				Vector2f v = (Vector2f) s.triggerBody().getPosition();
				sb.fill(255);
				sb.ellipse(v.x,v.y,20,20);
				if(s.triggerBody().getTouching().contains(bod)) {
					sb.println("SCENE JUMP!");
					dest = s.leaveD();
					sb.world.remove(s.triggerBody());
					cutControl = true;
					jump = s;
				}
			}
		}
		if(dest != null) {
			float oX =  bod.getPosition().getX();
			float oY = bod.getPosition().getY();
			float xDiff = dest.getX() - oX;
			float yDiff = dest.getY() - oY;
			float norm = (float) Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff, 2));
			//sb.println(xDiff + "," + yDiff + "," + norm);
			if(norm < 1) { //if we are less than one pixel away
				if(destNPC != null) {
					dir = destNPC.npc.tf();
					sb.triggerNPC(destNPC, savedAction);
					destNPC = null;
				}
				dest = null;
				
			}
			else {
				if(Math.abs(xDiff) > Math.abs(yDiff)) {
					if(xDiff > 0)
						dir = 2;
					else
						dir = 3;
				} else {
					if(yDiff > 0)
						dir = 1;
					else
						dir = 0;
				}
			}
			if(norm == 0)
				norm = 1;
			bod.adjustPosition(new Vector2f(xDiff/norm,yDiff/norm));
			sb.world.step();
			
			if(sb.world.getContacts(bod).length > 0) {
				
				//try moving in justX, justY
				float xnorm = Math.abs(xDiff);
				if(xnorm == 0)
					xnorm = 1;
				bod.setPosition(oX + xDiff/xnorm,oY);
				sb.world.step();
				
				if(sb.world.getContacts(bod).length > 0 || xnorm < 1) {
				
					float ynorm = Math.abs(yDiff);
					if(ynorm == 0)
						ynorm = 1;
					bod.setPosition(oX,oY + yDiff/ynorm);
					sb.world.step();
					if(sb.world.getContacts(bod).length > 0 || ynorm < 1) {
						dest = null;
						bod.setPosition(oX,oY);
					} else {
						if(yDiff > 0)
							dir = 1;
						else
							dir = 0;
					}
				} else {
					if(xDiff > 0)
						dir = 2;
					else
						dir = 3;
				}
			}
		} 
	}
	
	public void draw() {
		float x = bod.getPosition().getX();
		float y = bod.getPosition().getY();
		
		if(sb.showBounds) {
			sb.fill(255,0,0);
			sb.stroke(255);
			sb.strokeWeight(2);
			sb.ellipse(x,y,40,40);
		}
		
		sb.pushMatrix();
		sb.translate(x,y);
		
		sb.scale(sb.scene.getScale(x,y));
		if(dest != null)
			sb.image(walk[dir],-20,-65);
		else
			sb.image(stand[dir],-20,-65);
		sb.popMatrix();
	}

}
