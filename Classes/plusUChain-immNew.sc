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

+ MUChain {

	*immNew { |surface...args|
		var f = {
			//var ssa = "surface.size = %".format(surface.size).postln;
			var busses = ClusterArg( surface.size.collect(500 + _) );
			MUChain( *( args ++ [ [\pannerout, [\u_o_ar_0_bus, busses] ] ] ) ).releaseSelf_(false)
		};
		if(surface.isKindOf(PSurface).not) {
			Error("First argument of MUChain must be a PSurface").throw
		};
		^switch(ImmLib.mode)
		{\vbap} {
			f.()
		}
		{\previewHRTF}{
			f.()
		}
		{\previewStereo}{
			var points = ClusterArg( surface.pointsRV3D.collect{ |p| Point(p.x, p.y) } );
			MUChain( *( args ++ [ [\stereoOutput, [\point, points ] ] ] ) )
		}
	}

}
		