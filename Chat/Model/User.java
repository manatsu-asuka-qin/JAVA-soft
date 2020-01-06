package Chat.Model;

public class User {
    private String name;
    private String ipAdd;

    public User(String username){
        //构造函数，使用字符串
        String[]items = username.split("%");
        this.name =items[0];
        this.ipAdd = items[1];
    }

    public User(String name,String ipAdd){
        //构造函数，使用两个字符串
        this.name = name;
        this.ipAdd = ipAdd;
    }

    public String getName(){
        return name;
    }

    public String getIpAdd(){
        return ipAdd;
    }

    public String info(){
        //统一用 用户名+%+ip地址表示
        return name+"%"+ipAdd;
    }
}

