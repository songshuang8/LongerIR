package clurc.net.longerir.ircommu;

import java.util.ArrayList;
import java.util.List;

import clurc.net.longerir.data.RemoteInfo;

public class DesRemote {
    public String name;
    public int remoreid;
    public String[] pagename;
    public boolean hasshit;
    public List<DesRemoteBtn> btns = new ArrayList<DesRemoteBtn>();
    public List<RemoteInfo>src = new ArrayList<RemoteInfo>();

}
