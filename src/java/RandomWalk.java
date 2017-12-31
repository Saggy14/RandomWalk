/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 *
 * @author dhruv
 */

@WebServlet(urlPatterns = {"/RandomWalk"})
public class RandomWalk extends HttpServlet {
     String minSkill;
 
    int count = 0;
    int min=10;  //Min Cardinality of Experts
    int max=30;  //Max Cardinality of Experts
    int proj_no=3; //No. of Projects P{Si.....Sk}
    int nodes;
    
    List<String>item=new ArrayList<String>();
	
    HashMap<String,ArrayList<Integer>> skillExpertMap = new 	HashMap<String,ArrayList<Integer>>();
	
    DoubleMatrix2D n;	//NXN mMatrix
    DoubleMatrix2D A;   //Transition Matrix * beta
    double beta=0.8;
    double one_minus_beta=0.2;
    DoubleMatrix2D result;   //New M
    Map<Integer, ArrayList<Integer>> rowMap;  //Graph
    DoubleMatrix2D V1;  //V1=New M * (1/nodes)
    Map<String, Integer> bestTeam = null;
   
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    public void init() throws ServletException {
        minSkill = null;
}
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        
       
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        
	buildGraph();
        buildSkillSetExpertStruct();
	buildBestTeam();	
       
        request.setAttribute("message",bestTeam);
        request.setAttribute("msg", minSkill);
        request.getRequestDispatcher("/WEB-INF/RandomWalk.jsp").forward(request, response);
        request.removeAttribute("msg");
        
            /* TODO output your page here. You may use following sample code. */
            
           
        //response.setIntHeader("Refresh", 5);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        
        /**PrintWriter out = response.getWriter();
   
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RandomWalk</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("Start");
            out.println("</br>");
            response.setContentType("text/html;charset=UTF-8");
            out.println(minSkill);
            out.println(bestTeam);
            out.println(count);
            out.println("</br>");
            out.println("End");
            out.println("</body>");
            out.println("</html>");  */
            
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public void buildSkillSetExpertStruct() throws IOException
	{
		
		Random randomgenerator=new Random();
		
		ArrayList<String>EligibleSkills=new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\dhruv\\Desktop\\saga\\attachments\\SC_invertedTermCount.txt"));
		String line;
		while((line=br.readLine())!= null)
		{
			
			String cols[]=line.split("\t");
			String skill= cols[0];
			Integer Experts=Integer.parseInt(cols[1]);
			
			if(Experts >= min && Experts <= max)
			{	
				
				EligibleSkills.add(skill);
			}
		}
		br.close();
		
		//chose random skills for the project
		while(item.size() < proj_no) {
			int skillId = randomgenerator.nextInt(EligibleSkills.size());
			String skill = EligibleSkills.get(skillId);
			if(!item.contains(skill)) {
				item.add(skill);
			}
		}

		
		br = new BufferedReader(new FileReader("C:\\Users\\dhruv\\Desktop\\saga\\attachments\\SC_invertedTermMap.txt"));
		
		while((line=br.readLine())!= null)
		{	
			String cols[]=line.split("\t");
			String Skill=cols[0];
			if(item.contains(Skill))
			{	
				ArrayList<Integer> Num_Experts = new ArrayList<Integer>();
				for(int i=1;i<cols.length;i++)
				{
					int value=Integer.parseInt(cols[i]);
					Num_Experts.add(value);
				}
				
				skillExpertMap.put(Skill, Num_Experts);
			 }
		}
		br.close();
		
		return ;
	
	}
	
    public void buildBestTeam()
	{	
		//Get the skill with minimum cardinality
		for (String key : skillExpertMap.keySet())
		{
                
                    if (minSkill == null || skillExpertMap.get(key).size() < skillExpertMap.get(minSkill).size() )
		    {minSkill = key;}	
                    
		}
                
           
		//System.out.println(minSkill);
		
		double maxScore = Double.MIN_VALUE;
		
		
		int minSkillCardSize = skillExpertMap.get(minSkill).size();
		
		for(int i=0; i<minSkillCardSize; i ++) {
			
			Map<String, Integer> team = new HashMap<String, Integer>();
			double score = 0;
			
			int micCardExpertId = skillExpertMap.get(minSkill).get(i);
			team.put(minSkill, micCardExpertId);
			
			for(String skill : item) {
				if(!skill.equals(minSkill)) {
					
					ArrayList<Integer> restList = new ArrayList<Integer>();
					for(String coveredSkill : team.keySet()) {
						restList.add(team.get(coveredSkill));
					}
					
					//V1 stores the importance of each node
					RandomWalk(restList);
					
					Map<Integer, Integer> scoreExpertMap = new HashMap<Integer, Integer>();
					
					double max_expert_score = -1;
					int selected_expert = -1;
					
					for(int expertId : skillExpertMap.get(skill)  ) {
						double expert_score = V1.get(expertId, 0);
						if(expert_score > max_expert_score) {
							max_expert_score = expert_score;
							selected_expert = expertId;
						}
					}
					
					team.put(skill, selected_expert);
					score += max_expert_score;
					//System.out.println(max_expert_score);
				}
			}
			
			
			if(score > maxScore) {
				maxScore = score;
				bestTeam = team;
			}			
				
		}
                
           
               
	}
	
	public void RandomWalk(ArrayList<Integer> restList)
	{
		 //Creating 0.2(NxN) matrix on the basis of values in restList
		
		result = A.copy();
		
		for(int i=0;i<restList.size();i++) {
			
			int rowId = restList.get(i);
			
			for(int colId=0;colId<nodes;colId++) {
				double valueOfn = 1.0/restList.size();
				valueOfn = valueOfn*(one_minus_beta);
				double AValue = A.get(rowId, colId);
				double resultValue = valueOfn + AValue;
				result.set(rowId, colId, resultValue);
			}
			
		}
		
		
	   V1=new SparseDoubleMatrix2D(nodes,1);
	   double valueV1 = 1.0/nodes;
	   for(int i=0;i<nodes;i++)
	   {
		   //V1.set(i,0,Math.round(valueV1*10000.00)/10000.00);
		   V1.set(i,0,valueV1);
	   }
	   
	   Algebra algebra = new Algebra();
	   
	   for(int i=0;i<10; i ++) {		   
		   DoubleMatrix2D V2 = algebra.mult(result, V1);
		   V1 = V2;
	   }
	   
	   
	   return;
	}
        
        public void buildGraph() throws FileNotFoundException, IOException

	{
		
		rowMap = new HashMap<Integer,ArrayList<Integer>>();
		
		String sCurrentLine;
		try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\dhruv\\Desktop\\saga\\attachments\\SC_G1.txt")))
       {
	        while ((sCurrentLine = br.readLine()) != null) {
	
	            String[] cols = sCurrentLine.split("\t");
	            if (cols.length < 1) {
	            }
	            try {
	                int colKey = Integer.parseInt(cols[0]);
	               
	               ArrayList<Integer> colValues = new ArrayList<Integer>(cols.length);
	                
	                	for(int i=1;i<cols.length;i++)
	                		{
	                			colValues.add(Integer.parseInt(cols[i]));
	                			
	                		}
	                	rowMap.put(colKey, colValues);
	            }
	                
	                       
	            catch (NumberFormatException e) {}
	        }
	        
	        nodes = rowMap.size();
	        A=new SparseDoubleMatrix2D(nodes,nodes);
	        
	        try {
     
		        for(Integer node : rowMap.keySet())
		        {
		        	
			        for(Integer neighbours : rowMap.get(node))	
			        {
			        	double rowSum=rowMap.get(node).size();
			        	double value=(1/rowSum);
			        	value = value*beta;
			        	//A.set(neighbours, node, Math.round(value*10000.00)/10000.000);
			        	A.set(neighbours, node, value);
			        	
			        }
		        }
	        }
	        catch(Exception ex) {
	        	ex.printStackTrace();
	        	System.exit(0);
	        }
       }
        }
        
        public void destroy() {
        try {
           minSkill = null;
        } catch (Exception e) {
	    e.printStackTrace();
	}
    }
        
}
