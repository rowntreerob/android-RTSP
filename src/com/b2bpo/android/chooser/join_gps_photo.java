package com.b2bpo.android.chooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;

public class join_gps_photo {
	private static String filnme;
	private static String filnme2;
	public Fileprocess fp= null;
	
	join_gps_photo(String n1, String n2, String n3){
		 fp = new Fileprocess(n1, n2, n3);
	}
	
	class Fileprocess implements Comparator<Date>{
		private BufferedReader filA; //photo
		private BufferedReader filB; //gps
		private FileWriter filC; //lat lon photo
		private String path;
		private boolean needfilA;
		private boolean needfilB;
		
		DataP dataP=null;
		DataG dataG=null;
		Date dateA=null;
		Date dateB=null;
		String strA=null;
		String strB=null;
		


		
		public Fileprocess (String nameA, String nameB, String nameC){
			this.path = "c:/temp/f4/";
			needfilA = true;
			needfilB = true;
			try {
				filA = new BufferedReader(new FileReader(new File(new File(path), nameA)));
				filB = new BufferedReader(new FileReader(new File(new File(path), nameB)));
//				filC = new FileWriter(new File(new File(path), nameC));
				System.out.println(" construct new Fileprocess");
				process();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		
		/*
		 * filA  read
		 * filB  read
		 * filC 
		 * return -999 is both infiles at EOF
		 */
		private int getInput(){

			if (needfilA){
				// TODO handle EOF on A
				try {
					strA = filA.readLine();
					if (strA != null){ 
						
//						dataP = new DataP(filA.readLine());
						dateA = new Date(dataP.getTime());
						System.out.println("getinput rd A ");
					};
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			};
			
			if (needfilB){
				
				try {
					strB = filB.readLine();
					if (strB != null) {
						dataG = new DataG(filB.readLine());
						dateB = new Date(dataG.getTime());
						System.out.println("getinput rd B ");
					};
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			};
			if(strA == null && strB == null) return -999;
			if(strA == null) return 1;
			if(strB == null) return -1;
			return compare (dateA, dateB);
			
		}

		@Override
		public int compare(Date arg0, Date arg1) {
			// TODO Auto-generated method stub
			return arg0.compareTo(arg1);
		}
		
		public void process(){
			
			while (needfilA || needfilB){
			int flag = getInput();
			if (flag == -999) return;
			if (flag < 0){
				System.out.println(" LT in process");
				needfilB = false;
				needfilA = true;
			} else if ( flag == 0) {
				System.out.println(dataP.getAlbumId() +" " +dataG.getLat() + " " +dataG.getLon());
				needfilB = true;
				needfilA = true;
			} else if (flag > 0) {
				System.out.println(dataP.getAlbumId() +" " +dataP.getPhotoId() + " " +dataG.getLat() + " " +dataG.getLon());
				needfilB = true;
				needfilA = false;
			}}
			
		}
	}
		
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		join_gps_photo jgp = new join_gps_photo(args[0], args[1], args[2]);
		
		
	}
	
	public static void stub(Fileprocess fp ){
		fp.process();
	}


}
