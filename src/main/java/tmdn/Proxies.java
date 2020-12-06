package tmdn;

import java.util.ArrayList;
import java.util.List;

public class Proxies {

    private final List<String> proxies = get();

    int current = 0;

    public boolean hasNext(){
        return current < proxies.size() - 1;
    }

    public String getNext(){
        if (! hasNext())
        {
            throw new RuntimeException("no proxies left");
        }
        current++;
        return proxies.get(current);
    }

    private List<String> get(){
        List<String> proxies = new ArrayList<>();

        proxies.add("217.61.23.154:8080");
        proxies.add("51.141.83.47:3128");
        proxies.add("163.172.218.228:8181");
        proxies.add("13.69.75.191:3128");
        proxies.add("199.21.99.209:80");
        proxies.add("199.21.98.142:80");
        proxies.add("199.21.99.21:80");
        proxies.add("199.21.96.16:80");
        proxies.add("199.21.96.46:80");
        proxies.add("199.21.98.88:80");
        proxies.add("199.21.98.235:80");
        proxies.add("199.21.96.54:80");
        proxies.add("199.21.98.0:80");
        proxies.add("199.21.98.139:80");
        proxies.add("199.21.97.138:80");
        proxies.add("199.21.98.43:80");
        proxies.add("199.21.96.123:80");
        proxies.add("199.21.96.78:80");
        proxies.add("199.21.97.54:80");
        proxies.add("51.68.196.125:3128");
        proxies.add("31.186.101.10:3128");
        proxies.add("84.52.74.194:8080");
        proxies.add("199.21.97.194:80");
        proxies.add("199.21.97.161:80");
        proxies.add("199.21.96.169:80");
        proxies.add("199.21.98.137:80");
        proxies.add("199.21.96.32:80");
        proxies.add("199.21.96.224:80");
        proxies.add("134.209.195.94:8080");
        proxies.add("178.62.198.77:8080");
        proxies.add("185.97.252.54:3128");
        proxies.add("199.21.96.99:80");
        proxies.add("199.21.97.64:80");
        proxies.add("199.21.96.245:80");
        proxies.add("199.21.96.227:80");
        proxies.add("199.21.98.202:80");
        proxies.add("199.21.96.80:80");
        proxies.add("199.21.96.31:80");
        proxies.add("199.21.97.175:80");
        proxies.add("37.228.89.215:80");
        proxies.add("217.107.193.89:3128");
        proxies.add("94.142.142.32:3128");
        proxies.add("78.31.73.222:8080");
        proxies.add("62.33.207.196:3128");
        proxies.add("62.33.207.201:3128");
        proxies.add("86.62.78.4:8080");
        proxies.add("82.114.241.138:8080");
        proxies.add("91.214.70.99:3128");
        proxies.add("62.33.207.196:80");
        proxies.add("85.234.6.194:8081");
        proxies.add("199.21.98.130:80");
        proxies.add("199.21.96.48:80");
        proxies.add("199.21.98.212:80");
        proxies.add("199.21.97.25:80");
        proxies.add("199.21.96.111:80");
        proxies.add("199.21.97.164:80");
        proxies.add("199.21.99.208:80");
        proxies.add("199.21.99.79:80");
        proxies.add("199.21.97.20:80");
        proxies.add("199.21.99.15:80");
        proxies.add("199.21.96.163:80");
        proxies.add("199.21.98.76:80");
        proxies.add("199.21.96.28:80");
        proxies.add("199.21.99.10:80");

        return proxies;
    }
}
