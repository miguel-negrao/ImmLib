+ UScore {

	*immNew { |surface ... events|
		var m = surface.points.size;
		//^switch(ImmLib.mode)
		//vbap already has the preview modes:
		//{\vbap} {
		var busses = ClusterArg( m.collect(500 + _) );
		var ugroups = ClusterArg( m.collect({ |i| ("immGroup"++i).asSymbol }) );
		var panners;

		events.do( _.ugroup_( ugroups ) );

		panners = MUChain([\vbap3D_Simple_Panner,
			[\angles, surface.pointsDegrees, \spread, 0.0, \u_i_ar_0_bus, busses ]
		]).ugroup_(ugroups).addAction_('addToTail').hideInGUI_(true);

		^UScore(*(events++panners) ).cleanOverlaps
		/*}
		{\previewStereo}{
		UScore( *events ).cleanOverlaps
		}
		{\previewHRTF} {
		//not working yet.
		var busses = ClusterArg( m.collect(500 + _) );
		var ugroups = ClusterArg( m.collect({ |i| ("immGroup"++i).asSymbol }) );
		var panners, decoder;

		events.do( _.ugroup_( ugroups ) );

		panners = MUChain([\ambiEncode,
		[\angles, surface.pointsDegrees, \u_i_ar_0_bus, busses ]
		]).ugroup_(ugroups).addAction_('addToTail').hideInGUI_(true);

		UScore(*(events++panners) ).cleanOverlaps
		}*/
	}
}

/*
var testAudioMakePanners = { |surface|
	var p = ParameterSurfacePanners(surface);
	var out = p.makePanners.prepareAndStartIO( p.groupsForPanners ).collect{ p };
	if(ParameterSurfacePanners.preview){
		IO{ [~decoders, ULib.serversFlat].flopWith{ |decoder,s|
			SynthDef(\ambiDecode,{
				ReplaceOut.ar(0, FoaDecode.ar( In.ar(0,4), decoder) )
			}).play(target:s,args:nil,addAction:'addAfter');
		}} >>=| out
	}{ out }
};
*/