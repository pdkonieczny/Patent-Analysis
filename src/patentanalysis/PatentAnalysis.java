/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package patentanalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.swing.JOptionPane;
import java.util.regex.Pattern;
/**
 *
 * @author Philip Konieczny
 * 
 * This is the main program. It will provide the user an input option screen
 * where they can select:
 * 1. Print each acronym found in its own row. (Note this is MS ACCESS friendly)
 *      This feature creates a CSV file where each acronym found is inserted 
 *      into its own row as shown below:
 *      PatentNumber    Acronym     Count       Position Found      Patent Total
 *      122             ATM         3           1                   3
 *      122             NFL         5           2                   3
 *      122             BOB         1           3                   3
 *      133             ATM         2           1                   1
 * 
 * 2. Print each Patent in its own row
 *      This feature creates a CSV file where each patent has its own row as 
 *      shown below (count is in parens) and are ordered by appearance in file:
 *      Patent Number   All Acronyms
 *      122             ATM(3)  NFL(4)  ABC(1)  DEF(12)
 *      134             ATM(1)  BOB(7)  FINA(2) 
 * 
 * 3. Print whole word acronyms.
 *      This function takes the acronym library and searches the Patent file
 *      for all instances of the meaning of the acronyms (Ex: National Football
 *      League) and then prints them to a csv file with a format similar to #2:
 *      Patent Number   All Acronyms
 *      122             National Football League(1)     Auto Teller Machine(1)
 *      134             Bring on Bill(3)                Finally a nice apple(3)
 * 
 * 4. Update Acronym Library.
 *      Brings up a window will all known acronyms and allows the user to update
 *      the meaning of any of them. Then saves the library.
 *      
 * 
 * FUTURE IMPROVEMENT: 1.   Possibly add all acronyms ever found including 
 *                          unknown meanings???
 *                     2.   Add window to add/remove items from removal list
 *                     3.   Allow addition of new words to library
 *                     
 * 
 * 5. Exit Program
 * 
 */
public class PatentAnalysis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //list of words to be removed stored in removal_list.txt
        ArrayList<String> removal_list=new ArrayList<String>(); 
        //list of all found acronyms
        ArrayList<acronym> all_acronyms=new ArrayList<acronym>();
        //buffered readers and writers for files
        BufferedReader br = null;
        BufferedReader br2 = null;
        BufferedWriter bw= null;
        String str=null;
        //choice from original menu
        int Print_Columns = -1;
        //acronym library keeps list of all known meanings for acronyms
        ArrayList<acronym> acronyms_library=new ArrayList<acronym>();
        
        
        //options for original menu
        Object[] options = {"Print Acronym's Own Row",
                    "Print Patent's Own Row", "Find whole word acronyms","Update Acronym Library","Exit"};
        
        //original menu
        
        
        while(true){
        int n = JOptionPane.showOptionDialog(null,
        "Program Options",
        "Output format",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[1]);
        
        //user cancelled out
        if(n==-1){
            JOptionPane.showMessageDialog(null, "Exiting program","",1);
            continue;
        //print acronyms to file
        }else if (n<3){
            Print_Columns=n;
        //adjust acronym library
        } else if(n==3) {
            acronym_library_editor ale=new acronym_library_editor();
            ale.setVisible(true);
            while(!ale.closed){
                //wait until user closes library
            }
            continue;
        }else{
            System.exit(0);
        }
        
        
        //EVERYTHING AFTER WILL PRINT TO CSV FILE
            
            //get patent file name
            str = JOptionPane.showInputDialog(null, "Enter Name of Patent Text file : ","",1); 
            if(str==null || str.equals("")){
                continue;
            }else if(!str.endsWith(".txt")) { //add in .txt if not present
                str=str+".txt";
            }
            
            
            try { //file for removal list of words
                br2 = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\"+"Removal_List.txt"));
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "ERROR: Cannot find Removal_List.txt \nRESOLUTION: Please place this file in the same directory as the executable.\nACTION: Program will continue with no removing of common words. ","",1);
                br2=null;
            }

            
            try{ //read in parent text file
                File reopen=new File(System.getProperty("user.dir")+"\\"+str);
                FileReader fr=new FileReader(reopen);
                
                br = new BufferedReader(new FileReader(reopen));
                br.mark(5);
                int p=br.read();
                br.reset();
                //check to see what coding the file is in (ANSI or UTF)
                if(p!=80){
                    br.close();
                    br=null;
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.dir")+"\\"+str), "UTF-16"));
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "ERROR: Cannot find "+str+ "\nRESOLUTION: Please place this file in the same directory as the executable.\nACTION: Program will now exit. ","",1);
                System.err.println(e);
                continue;
            }
            
            //generate the file name for the CSV file
            // fileName_yyyyMMdd_hhmmss_patentoutput.csv
            try{ 
                String file_name_output=str.replaceAll(".txt", "_");
                Calendar cal = Calendar.getInstance();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_ hhmmss");
                
                bw = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"\\"+file_name_output+sdf.format(cal.getTime())+"_patentOutput.csv"));
                
                //csv column headers
                if(Print_Columns==0)
                    bw.write("Patent Number,Acronym,Count,Instance,Total Acronyms");
                else
                    bw.write("Patent Number, All Acronyms");
                bw.newLine();
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "ERROR: CANNOT create output CSV file.\nRESOLUTION: Please fix permissions of directory.\nACTION: Program will now exit.","",1);
                continue;
                
            }
            
            //read in acronym library for meanings
            BufferedReader br3;
            try{
                br3 = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\"+"acronym_lib.txt"));
                String s3="";
                while((s3=br3.readLine())!=null){
                    String[] parts = s3.split(":");
                    if(!s3.equals("")){
                        acronym m=new acronym(parts[0]);
                        m.set_meaning(parts[1]);
                        acronyms_library.add(m);
                    }
                }
                
                br3.close();
            }catch(Exception e){
                //JOptionPane.showMessageDialog(null, "ERROR: Cannot find acronym_lib.txt \nRESOLUTION: Please place this file in the same directory as the executable.\nACTION: Program will now exit. ","",1);
                //System.out.println("Error opening acronym_lib.txt \n" +e);
                
            }
            
            
            //parse through patent file
            try{
                String s2;
                String tot="";
                boolean last_time=false;
                String carryover=br.readLine(); //read in next line
                
                outer: while(!last_time){
                        tot=carryover; //start if there is carry over from last time
                        while(true){
                            if((s2=br.readLine())==null){ //null indicates end of file
                                last_time=true;
                                break;
                            }
                            else if(s2.startsWith("PatentNumber=")){ //found new patents so split and save in carryover
                                carryover=s2;
                                break;
                            }else if(!s2.equals("")) { //not blank line add to current patent
                                tot=tot+s2+"\n";
                            }
                        }
                        
                    if(br2!=null){ //if removal list exists then read in all entries
                        s2="";
                        while((s2=br2.readLine())!=null){
                            if(!s2.equals(""))
                                removal_list.add(s2);
                        }
                    }
                    
                    tot=tot.replaceAll("PatentNumber=",""); //replace the patentnumber= which should be at beginning
                   
                    String patentNumber=tot.substring(0, 7); //get patent number
                    
                    
                    
                    
                        //regex pattern matching meaning split on lookahead where there are 3,4,5,6 straight capital letters followed by non-cap
                        String [] test= tot.split("(?=([A-Z][A-Z][A-Z][^A-Z]|[A-Z][A-Z][A-Z][A-Z][^A-Z]|[A-Z][A-Z][A-Z][A-Z][A-Z][^A-Z]|[A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][^A-Z]))");
                       
                        //array of acronyms found
                        ArrayList<acronym> acronyms=new ArrayList<acronym>();
                        //splitting puts acronyms at beginning of entry
                        for(int i=0;i<test.length;i++){
                            
                            //non empty row, and row ends in capital
                            if(test[i].length()>1 && test[i].substring(test[i].length()-1).matches("[A-Z]")){
                               if(i!=0){//first row does not contain acronym
                                   
                                   //search row until non capital letter
                                   for(int t=0;t<test[i].length();t++){
                                       if((int)test[i].charAt(t)>64 && (int)test[i].charAt(t)<91){
                                          //upper case letter
                                       }
                                       else{
                                           //end of caps so know acronym now
                                           String temp=test[i].substring(0, t);
                                           if(!acronyms.contains(new acronym(temp))){
                                               acronyms.add(new acronym(temp)); //add to array
                                           }else{
                                               acronyms.get(acronyms.indexOf(new acronym(temp))).count++;
                                           }
                                           break;
                                       }
                                   }
                               }
                               //continues to delete until finds an entry not ending in a captial letter
                               int q=i+1; 
                               while(test[q].substring(test[q].length()-1).matches("[A-Z]")){ 
                                   test[q]="";
                                   q++;
                               }
                               test[q]="";
                               
                               //only one letter in row, add to one above it(bug fix since pattern matching causes a strange
                               // behavior when a 6 letter acronym is split into 4 rows EX: for ABCDEF
                               // DEF
                               //C
                               //B
                               //A
                               //so combine up to form back ABCDEF
                           }else if (test[i].length()==1){ 
                                   int q=i;
                               String s="";
                               while(test[q].length()==1){
                                    s=s+test[q];
                                   test[q]="";   
                                   q++;
                               }
                               test[q]=s + test[q];
                           //standard line starts with acronym
                           }else if(test[i].length()!=0 && i!=0){
                               for(int t=0;t<test[i].length();t++){
                                   if((int)test[i].charAt(t)>64 && (int)test[i].charAt(t)<91){

                                   }
                                   else{
                                       String temp=test[i].substring(0, t);
                                           if(!acronyms.contains(new acronym(temp))){
                                               acronyms.add(new acronym(temp));
                                           }else{
                                               acronyms.get(acronyms.indexOf(new acronym(temp))).count++;
                                           }
                                           break;
                                   }
                               }
                           }

                        }//end for loop
                        
                        //remove all words from removal list
                        if(br2!=null){
                            for(int w=0;w<removal_list.size();w++){
                                acronyms.remove(new acronym(removal_list.get(w)));
                            }
                        }
                        //print each acronym as a entry: patent number and acronym as for a database
                        if(Print_Columns==0){
                            for(int w=0;w<acronyms.size();w++){
                                bw.write(patentNumber+","+acronyms.get(w).name+","+acronyms.get(w).count+","+(w+1)+","+acronyms.size());
                                bw.newLine();
                                if(!all_acronyms.contains(new acronym(acronyms.get(w).name))){
                                    all_acronyms.add(new acronym(acronyms.get(w).name));
                                }
                            }
                        
                        //print each patent in its own row
                        }else if (Print_Columns==1){
                            bw.write(patentNumber);
                            for(int w=0;w<acronyms.size();w++){
                                bw.write(","+acronyms.get(w).name+"("+acronyms.get(w).count+")");
                                
                                if(!all_acronyms.contains(new acronym(acronyms.get(w).name))){
                                    all_acronyms.add(new acronym(acronyms.get(w).name));
                                }
                            }
                            bw.newLine();
                        
                        //print out number of times a meaning from library is matched
                        }else if(Print_Columns==2){
                            bw.write(patentNumber);    
                            for(int p=0;p<acronyms_library.size();p++){
                                Matcher m = Pattern.compile(acronyms_library.get(p).meaning).matcher(tot);

                                int count;
                                for (count = 0; m.find(); count++);
                                if(count!=0)
                                    bw.write(","+acronyms_library.get(p).meaning + "("+count+")");
                            }
                            bw.newLine();
                        }
                        
                    
            }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error generating list of patents and acronyms.","",1);
                br.close();
                if(br2!=null)
                    br2.close();
                System.err.println(ex);
                continue;
            } finally {
               
               br.close();
               if(br2!=null)
                    br2.close();
            }
            
            //update library with new additions from this patent file (increase entries in library)
             for(int i=0;i<all_acronyms.size();i++){
                        int index=acronyms_library.indexOf(all_acronyms.get(i));
                        if(index!=-1){                      
                            all_acronyms.get(i).set_meaning(acronyms_library.get(index).meaning);
                        } 
            }
            
            bw.newLine();
            

            br = null;
            br2 = null;


           
                //second pass through look for acronyms in parens (indicates meaning
            
            
                //open patent file
                try{
                    br = new BufferedReader(new FileReader(System.getProperty("user.dir")+"\\"+str));
                    br.mark(5);
                    int p=br.read();
                    br.reset();
                    //check whether its in ANSI or UTF 16
                    if(p!=80){
                        br.close();
                        br=null;
                        br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.dir")+"\\"+str), "UTF-16"));
                    }
                }catch(Exception e){
                    JOptionPane.showMessageDialog(null, "CANNOT FIND "+str+ "\nPlease place this file in the same directory as the executable. ","",1);
                    continue;
                }
                
                //split each on ()
                try{
                    String line="";
                    while((line=br.readLine())!=null){
                        if(!line.equals("")){
                          
                            
                            String [] test=line.split("(?=([(][A-Z][A-Z][A-Z][)]|[(][A-Z][A-Z][A-Z][A-Z][)]|[(][A-Z][A-Z][A-Z][A-Z][A-Z][)]|[(][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][)]))");
                            if(test.length>1){
                                
                                for(int i=1;i<test.length;i++){
                                    
                                    int m=1; //m is length of acronym
                                    while((int)test[i].charAt(m)>64 && (int)test[i].charAt(m)<91){
                                        m++;
                                    }
                                    
                                    //remove common words not part of definition
                                    test[i-1]=test[i-1].replaceAll("-"," ");
                                    test[i-1]=test[i-1].replaceAll(" and "," ");
                                    test[i-1]=test[i-1].replaceAll(" of "," ");
                                    test[i-1]=test[i-1].replaceAll(" to "," ");
                                    test[i-1]=test[i-1].replaceAll(" the "," ");
                                    test[i-1]=test[i-1].replaceAll(" or "," ");
                                    test[i-1]=test[i-1].replaceAll("\\s+", " ");
                                    
                                    //deliminate words
                                    String [] words= test[i-1].split(" ");
                                    String def="";
                                    //get previous m words for meaning of acronym
                                    for(int q=words.length-m+1;q<words.length;q++){
                                        if(q<0){
                                            continue;
                                        }
                                        def= def + " "+words[q];


                                        int loc=-1;
                                        //definition is already found
                                        if((loc=all_acronyms.indexOf(new acronym(test[i].substring(1, m))))!=-1){
                                            int match=all_acronyms.get(loc).check_meaning(def); //see how similar the match is

                                            //if better than previous best match
                                            if(match>all_acronyms.get(loc).match){
                                                //update list
                                                all_acronyms.get(loc).meaning=def;
                                                all_acronyms.get(loc).match=match;
                                            }
                                        }else{ //not found before so add
                                            all_acronyms.add(new acronym(test[i].substring(1, m)));
                                            int index=all_acronyms.indexOf(new acronym(test[i].substring(1, m)));
                                            all_acronyms.get(index).set_meaning(def);
                                        }
                                        
                                    }
                                    }   
                                
                                }
                        }
                        
                    }     
                    //update csv file with list of definitions/??? at bottom
                    for(int i=0;i<all_acronyms.size();i++){
                        if(all_acronyms.get(i).meaning.equals("")){
                            bw.write(all_acronyms.get(i).name + ",???" );
                        }else{
                            bw.write(all_acronyms.get(i).name + "," + all_acronyms.get(i).meaning);
                        }
                        bw.newLine();
                    }
                    
                    //update acronyms_library list
                    for(int i=0;i<all_acronyms.size();i++){
                        
                        if(!all_acronyms.get(i).meaning.equals("")){
                            int index=acronyms_library.indexOf(all_acronyms.get(i));
                            if(index!=-1){                      
                                acronyms_library.get(index).set_meaning(all_acronyms.get(i).meaning);
                            }else{
                                 acronyms_library.add(all_acronyms.get(i));
                            }
                            
                        }
                    }
                
                                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error generating list of acronyms conversion names.","",1);
                    br.close();
                    continue;
                } finally {
                    bw.close();
                    br.close();
          
                }
                
                //save back to library file
                try{
                    File f=new File(System.getProperty("user.dir")+"\\"+"acronym_lib.txt");
                    f.delete();
                    f.createNewFile();
                    BufferedWriter bw2 = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"\\"+"acronym_lib.txt"));
                    for(int i=0;i<acronyms_library.size();i++){
                        bw2.write(acronyms_library.get(i).name+ ":"+acronyms_library.get(i).meaning);
                        bw2.newLine();
                    }
                    bw2.close();
                
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error generating list of acronyms library.","",1);
                    br.close();
                  
                }
                
                JOptionPane.showMessageDialog(null, "Successfully analyzed patent file.","",1);
            
        } 
            
    }
}
