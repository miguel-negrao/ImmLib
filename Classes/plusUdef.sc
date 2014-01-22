+ Udef {
	loadRemote { |localDir, remoteDir|
		this.synthDef.writeDefFile(localDir);
		ULib.allServers.do{ |s|
			s.sendMsg("/d_load", (remoteDir ++"/"++ this.synthDef.name ++ ".scsyndef").postln)
		}
	}

	loadSonicLab {
		this.loadRemote("/run/user/1000/gvfs/sftp:host=sonic-lab.local/private/tmp", "/private/tmp")
	}
}

