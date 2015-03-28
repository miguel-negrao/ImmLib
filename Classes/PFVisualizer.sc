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
    classvar <currentPort = 30000,
    <binary = "pfVisualizer",
	<>zoom = 0.5,
	<>rotx = 275.0,
	<>roty = 180.0,
	<>rotz = 105.0;
    var <rendererAddr, <label="";
	var <connected = false;


	*basicNew{ |addr, label=""|
        currentPort = currentPort + 1;
		^super.newCopyArgs(addr, label ?? "")
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
		^PFVisualizer.fullBinaryPath.shellQuote++" "++rendererAddr.port++" "++label.shellQuote++" "++zoom++" "++rotx++" "++roty++" "++rotz;
    }

    startRenderer { |closeOnCmdPeriod = true|
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
			this.sendMsg("/quit");
			0.1.wait;
			this.connected_( false );
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
		^IO{
			fork{
				this.sendMsg("/quit");
				0.1.wait;
				this.connected_( false );
			};
		}
    }


}

PSmoothPlot : PFVisualizer {
	classvar <all;//:: IdentityDictionary Symbol PFVisualizer

    var <faces, <surface;

	*initClass {
		all = IdentityDictionary()
	}

    *basicNew { |type = \sphere, label, port|
		var faces = PGeodesicSphere.sphereFaces(2);
		var surface = PGeodesicSphereDual(2);
		port = port ?? { var x = currentPort; currentPort = currentPort + 1; x };
		^super.basicNew( NetAddr("localhost", port), label ?? "" ).init( faces, surface )
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

    init { |aFaces, aSurface|
        faces = aFaces;
        surface = aSurface;
    }

	sendGeometry {
		this.sendBundle(
			["/colors"]++(surface.pointsRV3D.collect{ |v| [0.0, v[2].linlin(-1.0,1.0,0.3,0.7), 0.0] }.flat),
			["/triangles"]++faces.flat,
		);
	}

    animate{ |pf...args| //args t, c1, c2, c3...
        var tEventSource = args[0].changes;
        var sendColors = { |v| IO{
			this.sendMsg(* (["/colors"]++([v,surface.points].flopWith{ |c,xs|
				//var x  = c.linlin(0.0,1.0,0.3,1.0);
				//[0.0, x, 0.0]
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
