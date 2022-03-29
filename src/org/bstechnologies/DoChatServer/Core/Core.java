package org.bstechnologies.DoChatServer.Core;

import org.bstechnologies.DoChatServer.TokenData.TokenGen;
import org.bstechnologies.DoChatServer.TokenData.TokenManager;
import org.bstechnologies.NetRequestManager.NetRequestManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Core {
    private TokenManager tokenManager;
    public Core(TokenManager TokenManager){this.tokenManager=TokenManager;}
    public String parse(String msg) throws Exception {
        NetRequestManager nrm = new NetRequestManager();
        nrm.parse(msg);
        String cmd = nrm.getMain();
        if(cmd.equals("newuser"))
        {
            String name = nrm.get("name");
            if(name==null){return "request?status=false&reason=invalid_information";}
            String passwd = nrm.get("passwd");
            if(passwd==null){return "request?status=false&reason=invalid_information";}
            String id = "";
            while (true)
            {
                TokenGen token = new TokenGen();
                id = token.genToken(4,false);
                File file = new File("data/users/"+id);
                if(!file.exists())break;
            }
            String authToken = new TokenGen().genToken(50);
            JSONObject json = new JSONObject();
            json.put("name",name);
            json.put("passwd",passwd);
            json.put("authToken",authToken);
            FileWriter fw = new FileWriter("data/users/"+id);
            fw.write(json.toJSONString());
            fw.close();
            return "request?status=true&id="+id+"&authToken="+authToken;
        }
        if(cmd.equals("login"))
        {
            String id = nrm.get("id");
            if(id==null){return "request?status=false&reason=insufficient_information";}
            String authToken = nrm.get("authToken");
            if(authToken==null){return "request?status=false&reason=insufficient_information";}
            JSONObject json = loadData("data/users/"+id);
            if(json==null){return "request?status=false&reason=no_id_found";}
            String authTokenData = json.get("authToken").toString();

            if(authToken.equals(authTokenData)){
                String token = "";
               while (true)
               {
                   String temp = new TokenGen().genToken(20);
                   boolean check = tokenManager.checkToken(temp);
                   if(check)continue;
                   token = temp;
                   break;
               }
               tokenManager.addToken(token,"account");
               return "request?status=true&token="+token;
            }
            else{
                return "return?status=false&reason=invalid_authToken";
            }

        }
        if(cmd.equals("getAuthToken"))
        {
            String id = nrm.get("id");
            if(id==null){return "request?status=false&reason=insufficient_data";}
            JSONObject json = loadData("data/users/"+id);
            if(json==null){return "request?status=false&reason=no_id_found";}
            String passwd = nrm.get("passwd");
            if(passwd==null){return "request?status=false&reason=insufficient_data";}

            String passwdSrc = json.get("passwd").toString();
            if(passwd.equals(passwdSrc))
            {
                String authToken = json.get("authToken").toString();
                return "request?status=true&authToken="+authToken;
            }
            else{
                return "request?status=false&reason=wrong_password";
            }
        }
        return null;
    }
    private JSONObject loadData(String path){
        try{
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(path));
            JSONObject json = (JSONObject) obj;
            return json;
        }catch(Exception e)
        {}
        return null;
    }

}
