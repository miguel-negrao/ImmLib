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
        servers = servers ? ULib.serversFlat;
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