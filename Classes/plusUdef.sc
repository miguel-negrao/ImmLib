/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

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
