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

		^UScore(*(events++panners) )
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