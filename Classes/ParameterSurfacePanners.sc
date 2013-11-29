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

ParameterSurfacePanners {
	classvar <>preview=false;
    var <surface, <servers;
    //internal
    var <chainGroups, <pannerGroups, <groupsForUnits, <groupsForPanners, <busses, <panners;

    *new{ |surface, servers|
        ^super.newCopyArgs(surface,servers).init
    }

    init {

        var indexesForPanners, distributeIndexes;
        var n = surface.points.size;
        servers = servers ? ULib.allServers;
        chainGroups = servers.collect{ |s| Group(s) };
        pannerGroups = chainGroups.collect{ |g| ParGroup(g, \addAfter) };

        indexesForPanners = (1..n).traverse({ |j|
            ST({ |b| Tuple2( (b+1).mod(servers.size), b) })
        }).evalState(0);

        distributeIndexes = { |array| indexesForPanners.collect{ |index| array[index] } };

        groupsForUnits = ClusterArg( distributeIndexes.(chainGroups).collect([_]) );
        groupsForPanners = ClusterArg( distributeIndexes.(pannerGroups) );

        busses = ClusterArg( n.collect(500 + _) );

    }

    makePanners{
		^if( preview.not ) {
			MU(\vbap3D_Simple_Panner,
				[\angles, surface.pointsDegrees, \spread, 0.0, \u_i_ar_0_bus, busses ]
			)
		} {
			MU(\ambiEncode,
				[\angles, surface.pointsDegrees, \u_i_ar_0_bus, busses ]
			)
		}
    }

    sendToPannersU {
        ^[\pannerout, [\u_o_ar_0_bus, busses] ]
    }


}