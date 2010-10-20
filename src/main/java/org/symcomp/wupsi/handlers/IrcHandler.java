package org.symcomp.wupsi.handlers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.symcomp.openmath.OpenMathBase;
import org.symcomp.scscp.SCSCPClient;
import org.symcomp.wupsi.Wupsifer;

/**
 *
 * @author hornp
 */
public class IrcHandler extends WupsiHandler {

    private Wupsifer w;
    private List<WupsiIrcConnect> wic;

    public IrcHandler(Wupsifer w) {
        this.w = w;
		this.wic = Collections.synchronizedList(new LinkedList<WupsiIrcConnect>());
    }

    public String command() {
        return "irc ";
    }

    public void handle(String in) {
		Pattern p = Pattern.compile("irc( )+" +
                "to( )+(([0-9A-Za-z\\_\\-\\.]+)(\\:([0-9]+))?)( )+" +
                "channel( )+([#0-9A-Za-z\\_\\-\\.]+)( )*" +
                "(nick ([A-Za-z][0-9A-Za-z\\_]*))?( )*" +
                "(system ([A-Za-z][0-9A-Za-z\\_]*))?");
		Matcher m = p.matcher(in);
		if (!m.matches()) {
			w.error("# syntax: " + help());
			return;
		}
		
        // The parsing result is:
        // Group 0: irc to irc.freenode.net:6667 channel #muff nick hornp system maple
        // Group 1:
        // Group 2:
        // Group 3: irc.freenode.net:6667
        // Group 4: irc.freenode.net
        // Group 5: :6667
        // Group 6: 6667
        // Group 7:
        // Group 8:
        // Group 9: #muff
        // Group 10:
        // Group 11: nick hornp
        // Group 12: hornp
        // Group 13:
        // Group 14: system maple
        // Group 15: maple

        // assemble the parameters
        String host    = m.group(4);
        int port       = m.group(6) == null ? 6667 : Integer.parseInt(m.group(6));
        String channel = m.group(9);
        String system  = m.group(15) == null ? w.getActiveClient() : m.group(15);
        String nick    = m.group(12) == null ? "wupsi" + system : m.group(12);

		//Get the client
		SCSCPClient client = w.getClient(system);
		if (client == null) {
			w.error("# Client with id '" + system + "' does not exist.");
			return;
		}

        wic.add(new WupsiIrcConnect(w, host, port, channel, nick, client));
    }

    public String help() {
        return "Attaches to an irc server:\nirc to <host>:<port> channel #<channel> nick <nick> system <systemid>";
    }

	@Override
    public void shutdownHook() {
        for (WupsiIrcConnect c : wic) {
            c.shutdown();
        }
    }

	@Override
	public void systemDiedHook(SCSCPClient client) {
		for (int i = wic.size() - 1; i >= 0; --i) {
			WupsiIrcConnect c = wic.get(i);
			if (c.client != null && c.client == client) {
				w.print("# Client died; shutting down IRC connection to " + c.desc());
				c.systemdied();
				wic.remove(i);
			}
		}
	}
	
	

    /**
	 * Treats IRC events. The most of them are just printed.
	 */
	public class WupsiIrcConnect implements IRCEventListener {

        Wupsifer w;
        String host;
        int port;
        String channel;
        String nick;
        SCSCPClient client;
        IRCConnection conn;
        Random rng;

        String[] answers = new String[]{ 
			"I think that's %s.",
            "Isn't that %s?",
            "That's %s, isn't it?",
            "hmmm... %s?",
            "That's roughly %s, I guess."
		};
		String[] errors = new String[]{
			"I'm sorry, I don't know!",
			"I have no idea what you are talking about :(",
			"Keine Ahnung!",
			"Ermmmm... I don't really know... Have you tried Google?"
		};
		String[] leavemsgs = new String[] {
			"I'm leaving now, see you.",
			"I'm sorry, I must be off now!",
			"kthxbye",
			"That's all, folks!"
		};
		String[] diedmsgs = new String[] {
			"You know when you're falling asleep, and you imagine yourself walking or something, and suddenly you misstep, stumble, and jolt awake? Well, that's what a segfault feels like."
		};
		String[] parseerrors = new String[]{
			"I'm sorry -- that question, I could not parse. Please learn to speak the Popcorn language!"
		};
		
		public String desc() {
            return host+":"+port+", channel "+channel+", nick "+nick;
		}

        public WupsiIrcConnect(Wupsifer w, String host, int port,
            String channel, String nick, SCSCPClient client) {
		
			this.rng = new Random();
	
            this.w = w;
            this.host = host;
            this.port = port;
            this.channel = channel;
            this.nick = nick;
            this.client = client;
            w.print("# Connecting to IRC server "+desc());

            // initialize the irc client
			//IRCConnection(String host, int portMin, int portMax, String pass, String nick, String username, String realname) 
            conn = new IRCConnection(host, new int[] { port },
                    null, nick, nick, "SCIEnce Wupsi");
            conn.addIRCEventListener(this);
            conn.setEncoding("UTF-8");
            conn.setPong(true);
            conn.setDaemon(false);
            conn.setColors(false);

			// connect
            try {
                conn.connect();
            } catch (IOException ex) {
                Logger.getLogger(IrcHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void shutdown() {
            conn.doPrivmsg(channel, leavemsgs[rng.nextInt(leavemsgs.length)]);
            conn.doQuit();
        }

		public void systemdied() {
            conn.doPrivmsg(channel, diedmsgs[rng.nextInt(diedmsgs.length)]);
            conn.doQuit();
		}

		public void onRegistered() {
			w.print("# Successfully connected to server, joining "+channel);
            try { Thread.sleep(1000); } catch (InterruptedException ignore) { }
            conn.doJoin(channel);
		}

		public void onDisconnected() {
        }

		public void onError(String msg) {
		}

		public void onError(int num, String msg) {
		}

		public void onInvite(String chan, IRCUser u, String nickPass) {
		}

		public void onJoin(String chan, IRCUser u) {
		}

		public void onKick(String chan, IRCUser u, String nickPass, String msg) {
		}

		public void onMode(IRCUser u, String nickPass, String mode) {
		}

		public void onMode(String chan, IRCUser u, IRCModeParser mp) {
		}

		public void onNick(IRCUser u, String nickNew) {
		}

		public void onNotice(String target, IRCUser u, String msg) {
		}

		public void onPart(String chan, IRCUser u, String msg) {
		}

		public void onPrivmsg(String chan, IRCUser u, String msg) {
			//If chan is equal to my own nick, it was a private message.
			boolean justme = chan.equals(nick);
			
			//Try to parse,
			OpenMathBase omb;
            try {
                omb = OpenMathBase.parse(msg);
            } catch (Exception ex) { 
				//Apparently, no correct question.
				if (justme) {
					conn.doPrivmsg(u.getNick(), parseerrors[rng.nextInt(parseerrors.length)]);
				}
				return; 
			}

			//Try to compute,
            if (null == client) return;
            OpenMathBase result = w.compute(client, omb);
            String answer;

            if (result.isError()) {
                answer = errors[rng.nextInt(errors.length)];
            } else {
	            String template = answers[rng.nextInt(answers.length)];
                answer = String.format(template, result.toPopcorn());
			}

			//and respond.
			if (justme) {
				//It was a private message, so we simply respond to the user.
				conn.doPrivmsg(u.getNick(), answer);
			} else {
				//Otherwise, we respond to the channel.
            	conn.doPrivmsg(channel, answer);
			}
		}

		public void onQuit(IRCUser u, String msg) {
		}

		public void onReply(int num, String value, String msg) {
		}

		public void onTopic(String chan, IRCUser u, String topic) {
		}

		public void onPing(String p) {
		}

		public void unknown(String a, String b, String c, String d) {
		}

	}



}
