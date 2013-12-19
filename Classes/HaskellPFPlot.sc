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

HaskellPFPlot {
    classvar <currentPort = 30000,
    <binary = "pfVisualizer";
    var <rendererAddr, <label="";

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
		^"%/../pfVisualizer/%/%".format(HaskellPFPlot.filenameSymbol.asString.dirname,
			thisProcess.platform.name, HaskellPFPlot.binary)
	}

    startCommand {
		^HaskellPFPlot.fullBinaryPath.shellQuote++" "++rendererAddr.port++" "++label;
    }

    startRenderer { |closeOnCmdPeriod = true|
        this.startCommand.postln.unixCmd;
        if( closeOnCmdPeriod ) {
            CmdPeriod.doOnce({ this.stopRenderer })
        };
    }

    startRendererIO {
        ^this.startCommand.unixCmdIO
    }

    stopRenderer {
        rendererAddr.sendMsg("/quit");
    }

    stopRendererIO {
        ^rendererAddr.sendMsgIO("/quit")
    }

    *killall {
       this.killAllCommand.unixCmd
    }

    *killallIO {
        ^this.killAllCommand.unixCmdIO
    }

    *killAllCommand {
        ^"killall pfVisualizer"
    }

}


/*
x = ParameterFieldPlot2( \sphere, "test"  );
x.startRenderer
x.stopRenderer
*/
PSmoothPlot : HaskellPFPlot {
    var <faces, <surface;

    *basicNew { |type = \sphere, label|
        var faces = PSurface.sphereFaces(2);
        var points = faces.collect{ |vertices| vertices.sum / 3 };
        var surface = PSurface.sphericalGeometry( points.collect{ |p|
            p.asUnitSpherical.storeArgs
        } );
        currentPort = currentPort + 1;
		^super.basicNew( NetAddr("localhost", currentPort), label ?? "" ).init( faces, surface )
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

    startRenderer { |closeOnCmdPeriod = true|
        fork{
            super.startRenderer(closeOnCmdPeriod);
            1.wait;
            faces.clump(150).do{ |faces,i|
                rendererAddr.sendMsg(* ([if(i==0){"/triangles"}{"/add_triangles"}]++faces.flat))
            };
        }
    }

    startRendererIO {
        ^IO{ this.startRenderer }
    }

    animate{ |pf...args| //args t, c1, c2, c3...
        var tEventSource = args[0].changes;
        var sendColors = { |v| IO{
			rendererAddr.sendMsg(* (["/colors"]++([v,surface.points].flopWith{ |c,xs|
				var x  = c.linlin(0.0,1.0,0.3,1.0);
				[0.0, x, 0.0]
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
                //set greem to corresponding intensity
                [0.0, v2.linlin(0.0,1.0,0.3,1.0), 0.0]
            }.flat;
            rendererAddr.sendMsg( *msg )
        } };
        ^( (sendColors.(_) <%> pf.valueS( *([surface]++args) )) <@ tEventSource).reactimate
    }

}


/*
x = ParameterGridPlot( ParameterSurface.geodesicSphere );
x.startRenderer
x.stopRenderer
*/
PGridPlot : HaskellPFPlot {
    var <points;

    *basicNew { |surface, label|
        var points = surface.pointsRV3D.collect{ |x| x.asArray }.flat;
        ^super.basicNew( NetAddr("localhost", currentPort), label ).init( points )
    }

    *animate{ | sig, label|
        var plot = this.basicNew(ImmDef.currentSurface, label);
         ^Writer(Unit, T([],[],[ plot.startRendererIO ]) ) >>=|
        plot.animate(sig)

    }

    init { |aPoints|
        points = aPoints
    }

    startRenderer { |closeOnCmdPeriod = true|
        fork{
            super.startRenderer(closeOnCmdPeriod);

            1.wait;

            rendererAddr.sendMsg(* (["/cubes"]++points) )
        }
    }

    startRendererIO {
        ^IO{ this.startRenderer }
    }

    animate{ |sig|
		^sig.sampleOn(ImmDef.currentTimeES).collect{ |vs|
            IO{
                rendererAddr.sendMsg(*(["/colors"]++vs.collect{ |v|
                [0.0,v.linlin(0.0,1.0,0.3,1.0),0.0]
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


















