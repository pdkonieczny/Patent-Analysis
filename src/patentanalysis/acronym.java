/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package patentanalysis;

/**
 *
 * @author Philip Konieczny
 * This class is used to hold the acronym/ meaning/ match strength
 */
public class acronym{
    public String name;
    public int count;
    public String meaning;
    public int match;
    
    //create acronym
    public acronym(String s){
        name=s;
        count=1;
        meaning="";
        match=0;
    }
    /*
     * Set meaning will determine the match strength of the meaning bu comparing
     * the acroynm to the first letter of each word
     * EX: ATM Automated Teller Mchine would be a match strength of 3
     * Ex2: NFL National Baseball League would be a match strength of 2 since only 2 letters match
     */
    public void set_meaning(String s){
        meaning=s;
        String[] words=meaning.split(" ");
        for(int i=1;i<words.length;i++){
         
            if(words[i].substring(0,1).toUpperCase().equals(name.substring(i-1,i))){
                match++;
            }
        }
    }
    
    /*
     * check meaning only checks the strength of the match 
     * returns an integer = to the strength of the match
     */
    public int check_meaning(String meaning2){
        String[] words=meaning2.split(" ");
        int returner=0;
        for(int i=1;i<words.length;i++){
            
            if(words[i].substring(0,1).toUpperCase().equals(name.substring(i-1,i))){
                returner++;
            }
        }
        return returner;
    }

    @Override
    /*
     * compares two acronyms on the name
     */
public boolean equals(Object o){
    acronym s = (acronym)o;
    
    if(s.name.equals(this.name)){
        return true;
    } 
    
    return false;
           
        
    
}
    @Override
    /*
     * simple print of acronym
     */
public String toString(){
    return name + ":"+match+":"+meaning;
}
}
