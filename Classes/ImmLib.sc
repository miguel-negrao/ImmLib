/*
    ImmLib
    Copyright 2013 Miguel Negrao.

    ImmLib: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

ImmLib {
	//previewStereo, previewHRTF
	classvar <>mode = \vbap;

	*baseDirectory{
		^ImmLib.filenameSymbol.asString.dirname++"/.."
	}

	*extraDefs {
		^[this.baseDirectory++"/UnitDefs"]
	}

	*startupStereo {

			var options = VBAPOptions(
				serverDescs: [["single","localhost", 57456]],
				device: nil,
				numOutputChannels: 48,
				angles: VBAPOptions.speakerPresets[\soniclab][\angles],
				distances: VBAPOptions.speakerPresets[\soniclab][\dists],
				loadDefsAtStartup: true,
				sendSynthDefsAtStartup: false,
				loadUdefViaRemoteFolder: false,
				remoteFolderForLoading: "",
				isSlave: false,
				extraDefFolders: false
			).extraDefFolders_( this.extraDefs );
			VBAPLib.previewMode = \stereo;
			Udef.loadOnInit = true;
			VBAPLib.startupR( options );

	}

	*startupSonicLabTest {
		var options;
		options = VBAPOptions.fromPreset(\soniclabTest)
		.extraDefFolders_( this.extraDefs );
		Udef.loadOnInit = true;
		VBAPLib.startupR( options );
	}

	*startupSonicLab {
		var options = VBAPOptions.fromPreset(\soniclab)
		.extraDefFolders_( this.extraDefs );
		Udef.loadOnInit = true;
		VBAPLib.startupR( options );
	}

	*recWindow {
		var q = ();

		q.recServer = ULib.allServers.at(0);

		q.pathGui = TextView().string_("/tmp/test1.aiff");

		q.bitGui = PopUpMenu().items_(["int24","int16","float"]).value_(0);

		q[\play] = Button().states_([["record"],["stop recording"]]).action_{ |v|

			if(v.value == 1) {
				//start
				"recording".postln;
				q.recSynth = Synth.basicNew("rec-ins", q.recServer);
				q.recBuf = Buffer.new(q.recServer, 65536, 32);
				q.recBuf.alloc(
					q.recBuf.writeMsg(q.pathGui.string, "AIFF", q.bitGui.item.postln, 0, 0, true,
						completionMessage:
						q.recSynth.newMsg(q.recServer, ["bufnum", q.recBuf], 'addToTail')
					)
				)
			} {
				//stop
				q.recSynth.free;
				q.recBuf.close(completionMessage:q.recBuf.freeMsg);
			}
		};

		q.w = Window.new("ImmLib record", Rect(200, 300, 700, 100))
		.layout_(
			VLayout(
				q.pathGui,
				HLayout(q[\play], q.headerGui, q.bitGui)
			)
		);

		q.w.front;
		^q
	}

}