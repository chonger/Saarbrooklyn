package saarbrooklyn;

import net.phys2d.raw.Body;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.shapes.Circle;
import pointclick.DialogGNode;
import pointclick.NPC;

public class SBDialogGraph {

	Saarbrooklyn sb;
	NPC npc;
	DialogGNode[] graph;
	Body[] nodeCirc;
	
	float x;
	float y;
	
	float nodeRadius = 20;
	
	public SBDialogGraph(Saarbrooklyn sb, NPC npc) {
		this.sb = sb;
		this.npc = npc;
		graph = npc.buildGraph();
		nodeCirc = new Body[graph.length];
		for(int i=0;i<graph.length;++i) {
			nodeCirc[i] = new StaticBody(new Circle(nodeRadius));
			nodeCirc[i].setPosition(i, i);
		}
		x = 0;
		y = 0;
	}
	
	public void draw() {
		for(int i=0;i<graph.length;++i) {
			DialogGNode n = graph[i];
			Body b = nodeCirc[i];
			float nX = b.getPosition().getX();
			float nY = b.getPosition().getY();
			
		}
	}
	
}
