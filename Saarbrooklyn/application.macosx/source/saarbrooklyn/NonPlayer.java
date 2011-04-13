package saarbrooklyn;

import gifAnimation.Gif;
import net.phys2d.raw.Body;
import pointclick.NPC;
import processing.core.PImage;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.shapes.Box;

public class NonPlayer {

	NPC npc;
	PImage[] imgs;
	Saarbrooklyn sb;
	int animIndex = 0;
	
	public NonPlayer(Saarbrooklyn sb, NPC npc) {
		this.npc = npc;
		this.sb = sb;
		imgs = new PImage[npc.imgs().length];
		for(int i=0;i<imgs.length;++i){
			if(npc.imgs()[i].indexOf(".gif") > 0) {
				imgs[i] = new Gif(sb,npc.imgs()[i]);
			} else {
				//sb.println("npcIMGI " + npc.imgs()[i]);
				imgs[i] = sb.loadImage(npc.imgs()[i]);
			}
		}
		sb.world.add(npc.bod());
	}
	
	public void draw() {
		//sb.println("drawing-" + npc.name() + " at ind " + npc.imgInd());
		sb.image(imgs[npc.imgInd()], npc.imgX(), npc.imgY());
		
		
		if(sb.showBounds) {
			sb.fill(0,0,255);
			sb.noStroke();
			sb.rect(npc.x() + npc.ox() - npc.cw()/2,npc.y() + npc.oy() - npc.ch()/2,npc.cw(),npc.ch());
			
			sb.fill(255,0,0);
			sb.stroke(0,255,0);
			sb.strokeWeight(2);
			sb.ellipse(npc.x() + npc.tx(), npc.y() + npc.ty(), 10, 10);
			float xx = 0;
			float yy = 0;
			switch(npc.tf()) {
			case 0: yy = -10; break;
			case 1: yy = 10; break;
			case 2: xx = 10; break;
			case 3: xx = -10; break;
			}
			sb.fill(255,0,0);
			sb.ellipse(npc.x() + npc.tx() + xx , npc.y() + npc.ty() + yy, 5, 5);
		}
	}
	
}
