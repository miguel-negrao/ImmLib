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

PFVisualizer {
    classvar <>currentPort = 30000,
    <binary = "pfVisualizer";

    var <rendererAddr, <label="";
	var <>zoom = 0.5,
	<>rotx = 275.0,
	<>roty = 180.0,
	<>rotz = 105.0,
	<>width = 500,
	<>height = 500;

	var <connected = false;


	*basicNew{ |addr, label="", zoom = 0.5, rotx = 275.0, roty = 180.0, rotz = 105.0, width = 800, height = 800|
        currentPort = currentPort + 1;
		^super.newCopyArgs(addr, label ?? "", zoom, rotx, roty, rotz, width, height)
    }

	*enAnimate{ |...args|
		^ENDef.appendToResult( this.animate( *args ) );
	}

	//alias
	*new{ |...args|
		^ENDef.appendToResult( this.animate( *args ) );
	}

	*fullBinaryPath {
		^"%/../pfVisualizer/%/%".format(PFVisualizer.filenameSymbol.asString.dirname,
			thisProcess.platform.name, PFVisualizer.binary)
	}

    startCommand {
		^PFVisualizer.fullBinaryPath.shellQuote++" -p"++rendererAddr.port++" -o"++zoom++" -x"++rotx++" -y"++roty++" -z"++rotz++" -w"++width++" -h"++height++" --l "++label.shellQuote;
    }

    startRenderer { |closeOnCmdPeriod = true, connectedAction|
		var t, f;
		this.startCommand.unixCmd;
		t = Process.elapsedTime;
		f = {
			|attempts|
			try {
				rendererAddr.connect;
				this.connected_( true );
				"Connected to pfVisualizer at port %, waited %s.".format(rendererAddr.port, (Process.elapsedTime-t).round(0.001)).postln;
				this.sendGeometry;
				if(connectedAction.notNil) { connectedAction.value };
				if( closeOnCmdPeriod ) {
					CmdPeriod.doOnce({ this.stopRenderer })
				};
			} {
				|err|
				if (err.isKindOf(PrimitiveFailedError) and: { err.failedPrimitiveName == '_NetAddr_Connect'}) {
					if(attempts > 0){
						0.01.wait;
						f.value(attempts - 1)
					}{
						"PFVisualizer class: could not connect to pfVisualizer process via tcp at port %.".format(rendererAddr.port).warn
					}
				} {

					err.throw;
				}
			}
		};

		fork{ f.(1000) };
    }

	sendGeometry { }

	sendBundle { |...args|
		if(connected) {
			try{ rendererAddr.sendBundle(nil, *args) }{ this.connected_( false ) }
		}
	}

	sendMsg { |...args|
		if(connected) {
			try{ rendererAddr.sendMsg(*args) }{ this.connected_( false ) }
		}
	}

    stopRenderer {
        fork{
			if( this.connected ) {
				this.sendMsg("/quit");
				0.1.wait;
				this.connected_( false )
			}
		};
    }

    *killall {
       this.killAllCommand.unixCmd
    }

    *killAllCommand {
        ^"killall pfVisualizer"
    }

	connected_ { |bool|
		connected = bool;
		if( connected.not ) {
			try{ rendererAddr.disconnect }
		};
		this.changed(\connected, connected)
	}

	//methods returning IO
    *killallIO {
        ^this.killAllCommand.unixCmdIO
    }

	startRendererIO {
		^IO{ this.startRenderer }
    }

    stopRendererIO {
		^IO{ this.stopRenderer }
    }


}

PSmoothPlot : PFVisualizer {
	classvar <all;//:: IdentityDictionary Symbol PFVisualizer

    var <triangles, <surface;

	*initClass {
		all = IdentityDictionary()
	}

    *basicNew { |type, label, port, zoom = 0.5, rotx = 275.0, roty = 180.0, rotz = 105.0|
		var triangles;
		var surface;
		var underlyingSurface = ImmDef.currentSurface;
		type ?? {
			var s = ImmDef.currentSurface;
			if( s.isKindOf(PSphericalSurface) ){
				type = \sphere
			};
			if( s.isKindOf(PPlane) ) {
				type = \plane
			}
		};
		switch(type)
		{\sphere}{
			triangles = PGeodesicSphere.sphereFaces(2);
			surface = PGeodesicSphereDual(2);
			zoom = 0.5;
			rotx = 275.0;
			roty = 180.0;
			rotz = 105.0;
		}
		{\plane}{
			//when testing outside FRP graph we need to have a test PPlane
			var y = underlyingSurface ?? { PPlane(RealVector3D[1.0, 1.0, 1.0],  RealVector3D[0.0,0.0,-2.0], RealVector3D[0.0,-2.0,0.0], 4, 6 ) };
			var n = 13;
			var dxn = y.dx.normalize;
			var dyn = y.dy.normalize;
			var points;
			var pointsWrapped;
			var z = n.collect{ |i|
				n.collect{ |j|
					var ii = i.linlin(0,n,0.0,1.0);
					var jj = j.linlin(0,n,0.0,1.0);
					var lx = y.dx.norm/n;
					var ly = y.dy.norm/n;
					var o = y.origin + (y.dx * ii) + (y.dy * jj);
					var lxp = [1/n,0];
					var lyp = [0,1/n];
					var op = [ii, jj];
					[
						[
							[op, op + lxp, op + lyp],
							[op + lxp + lyp, op + lxp, op + lyp]
						]
						,
						[
							[o, o + (dxn*lx), o + (dyn*ly)],
							[o + (dxn*lx) + (dyn*ly), o + (dxn*lx), o + (dyn*ly)]
						]
					]
			}}.flatten.flop;
			triangles = z[1].flatten;
			points = z[0].flatten.collect{ |x| x.sum / 3 };
			pointsWrapped = triangles.collect{ |x| x.sum / 3 };
			surface = PSurface( PPlaneM( y.origin, y.dx, y.dy), points, pointsWrapped);
			zoom = 0.9;
			rotx = 270;
			roty = 180;
			rotz = -90;
		};
		port = port ?? { var x = currentPort; currentPort = currentPort + 1; x };
		^super.basicNew( NetAddr("localhost", port), label ?? "", zoom, rotx, roty, rotz ).init( triangles, surface )
    }

	*off{ |pf...args|
		^pf.(*args)
	}

	*proxy{ |key, pf...args|
		var x = all.at(key);
		var result = if( x.isNil ) {
			var plot = this.basicNew;
			all.put(key, plot);
			CmdPeriod.doOnce({ all.put(key,nil) });
			Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
			plot.animate(pf, *args)
		} {
			x.sendGeometry;
			x.animate(pf, *args)
		};
		^ENDef.appendToResult( result )
	}

    *animate{ |pf...args|
        var plot = this.basicNew;
        ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animate(pf, *args)
    }

	*animateOnly{ |pf...args|
        var plot = this.basicNew;
        ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animateOnly(pf, *args)
    }

	*reusable{
		var plot = PSmoothPlot.basicNew;
		var x1 = plot.startRenderer;
		var doFunc = { |x| x.unsafePerformIO };
		var sendColors = { |v| IO{
			plot.sendMsg(* (["/colors"]++([v,plot.surface.points].flopWith{ |c,xs|
				[0.0, c.asFloat, (1-c).asFloat]
			}.flat)))
		}
		};
		var oldSig;
		^{ |pf...args|
			var tEventSource = args[0].changes;
			var outSignal = pf.(*args);
			var outSignal2 = (sendColors.(_) <%> pf.valueS(*([plot.surface]++args[0..]) ) );
			//actually I think I can just use enOut here... check later.
			if(oldSig.notNil) { oldSig.stopDoing(doFunc) };
			oldSig = (outSignal2  <@ tEventSource);
			oldSig.do(doFunc);
			outSignal
		};
	}

    init { |aTriangles, aSurface|
        triangles = aTriangles;
        surface = aSurface;
    }

	sendGeometry {
		this.sendBundle(
			["/colors"]++(surface.pointsRV3D.collect{ |v| [0.0, v[2].linlin(-1.0,1.0,0.3,0.7), 0.0] }.flat),
			["/triangles"]++triangles.flat,
		);
	}

    animate{ |pf...args| //args t, c1, c2, c3...
        var tEventSource = args[0].changes;
        var sendColors = { |v| IO{
			this.sendMsg(* (["/colors"]++([v,surface.points].flopWith{ |c,xs|
				[0.0, c.asFloat, (1-c).asFloat]
			}.flat)))
            }
        };
        var outSignal = pf.(*args);
		var outSignal2 = (sendColors.(_) <%> pf.valueS(*([surface]++args[0..]) ) );
        ^(outSignal2  <@ tEventSource).reactimate.collect{ outSignal }
    }

    animateOnly { |pf...args| //args t, c1, c2, c3...
        var tEventSource = args[0].changes;
        var sendColors = { |v| IO{
            var msg = ["/colors"]++v.collect{ |v2|
                //set green to corresponding intensity
                //[0.0, v2.linlin(0.0,1.0,0.3,1.0), 0.0]
				[0.0, v.asFloat, (1-v).asFloat]
            }.flat;
            rendererAddr.sendMsg( *msg )
        } };
        ^( (sendColors.(_) <%> pf.valueS( *([surface]++args) )) <@ tEventSource).reactimate
    }

}

PGridPlot : PFVisualizer {
	classvar <all;//:: IdentityDictionary Symbol PFVisualizer

	var <points;

	*initClass {
		all = IdentityDictionary()
	}

    *basicNew { |surface, label|
        var points = surface.pointsRV3D.collect{ |x| x.asArray }.flat;
        ^super.basicNew( NetAddr("localhost", currentPort), label ).init( points )
    }

	*proxy{ |key, sig, label|
		var x = all.at(key);
		var result = if( x.isNil ) {
			var plot = this.basicNew(ImmDef.currentSurface, label);
			all.put(key, plot);
			CmdPeriod.doOnce({ all.put(key,nil) });
			Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
			plot.animate(sig)
		} {
			x.sendGeometry;
			x.animate(sig)
		};
		ENDef.appendToResult( result );
	}

    *animate{ | sig, label|
        var plot = this.basicNew(ImmDef.currentSurface, label);
         ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animate(sig)

    }

    init { |aPoints|
        points = aPoints
    }

	sendGeometry {
		this.sendMsg(* (["/cubes"]++points) )
	}

    animate{ |sig|
		this.checkArgs("PGridPlot","animate",[sig],[FPSignal]);
		^sig.sampleOn(ImmDef.currentTimeES).collect{ |vs|
            IO{
                this.sendMsg(*(["/colors"]++vs.collect{ |v|
					[0.0, v.asFloat, (1-v).asFloat]
            }.flat) )
            }
        }.reactimate
    }

}

PHemiPlot {

	*animate{ |surface, sig|
		var values = surface.points.collect{ 1.0 };
		var points = surface.pointsRV3D.collect{ |r|
			T( r.z < 0, Point(r.x, r.y) )
		};
		var w = Window("Dual Hemisphere Plot", Rect(100, 200, 700, 300));
		var p1 = (w.bounds.width/4)@(w.bounds.height/2);
		var p2 = (w.bounds.width/4*3)@(w.bounds.height/2);
		var radius = w.bounds.height/2;
		var p3 = Point(radius,radius);
		var centers = points.collect{ |t|
			if(t.at1) {
				p1 +
				(p3 * t.at2)
			} {
				p2 +
				(p3 * t.at2)
			}
		};
		w.view.background_(Color.white);
		w.drawFunc = { |v|
			Pen.color = Color.green(0.1,0.5);
			Pen.addWedge(
				p1,
				radius,
				0,
				2*pi
			);
			Pen.fill;
			Pen.addWedge(
				p2,
				radius,
				0,
				2*pi
			);
			Pen.fill;
			"South".drawAtPoint(Point(10,10),color:Color.black);
			"North".drawAtPoint(Point(w.bounds.width/2+10,10),color:Color.black);
			[centers, values ].flopWith{ |c, v|
				Pen.color = Color.yellow(v);
				Pen.addWedge(
					c,
					10,
					0,
					2*pi
				);
				Pen.fill;
			}


		};
		^Writer(Unit, T([],[],[IO{
			w.front;
			CmdPeriod.doOnce{ if(w.isClosed.not){ w.close } };
		}]) ) >>=|
		sig.sampleOn(ImmDef.currentTimeES).collect{ |xs| IO{
			values = xs;
			{ w.refresh }.defer;
		} }.reactimate;
	}

	*enAnimate{ |...args|
		^ENDef.appendToResult( this.animate( *[ImmDef.currentSurface]++args ) );
	}

	*new {|...args|
		^ENDef.appendToResult( this.animate( *[ImmDef.currentSurface]++args ) );
	}
}
