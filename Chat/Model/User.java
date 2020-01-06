package Chat.Model;

public class User {
    private String name;
    private String ipAdd;

    public User(String username){
        //���캯����ʹ���ַ���
        String[]items = username.split("%");
        this.name =items[0];
        this.ipAdd = items[1];
    }

    public User(String name,String ipAdd){
        //���캯����ʹ�������ַ���
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
        //ͳһ�� �û���+%+ip��ַ��ʾ
        return name+"%"+ipAdd;
    }
}

