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

	*sonicLabTest {
		var options;
		options = VBAPOptions.fromPreset(\soniclabTest)
		.extraDefFolders_( this.extraDefs );
		Udef.loadOnInit = true;
		VBAPLib.startupR( options );
	}

	*sonicLab {
		var options = VBAPOptions.fromPreset(\soniclab)
		.extraDefFolders_( this.extraDefs );
		Udef.loadOnInit = true;
		VBAPLib.startupR( options );
	}

}