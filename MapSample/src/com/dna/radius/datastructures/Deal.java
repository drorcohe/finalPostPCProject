package com.dna.radius.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * represents a deal which was used by the owner before.
 * should be displayed in the deal history list.
 *
 */
public class Deal implements Serializable {
	
	private static final long serialVersionUID = -9140388376492045296L;
	
	
	private String id;
	private String dealContent;
	private int numOfLikes;
	private int numOfDislikes;
	private Date date;
	private ArrayList<Comment> dealComments;
	
	public Deal(String id, String content,
			int numOfLikes, int numOfDislikes, Date date) {
		
		this.id = id;
		this.dealContent = content;
		this.numOfLikes = numOfLikes;
		this.numOfDislikes = numOfDislikes;
		this.date = date;
		
		this.dealComments = new ArrayList<Comment>();
		
	}
	
	public String getId() { return id; }
	
	public String getDealContent() { return dealContent; }
	
	public int getNumOfDislikes() { return numOfDislikes; }
	
	public int getNumOfLikes() { return numOfLikes; }
	
	public Date getDealDate() { return date; }
	
	public void setNumOfLikes(int numOfLikes) { this.numOfLikes = numOfLikes; }
	
	public void setNumOfDislikes(int numOfDislikes) { this.numOfDislikes = numOfDislikes; }
	
	public void addComment(Comment newComment) { dealComments.add(newComment); }
	
	// TODO - needed?
	public ArrayList<Comment> getComments() { return dealComments; }
	
	//TODO - needed?
	public void setComments(ArrayList<Comment> comments) { this.dealComments = comments; }
	
	
}
