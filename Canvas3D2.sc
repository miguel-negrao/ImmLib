//lijon 2011

// TODO:
// *sphere, *pyramid, etc..?
// morphing?

/*
for(float theta=0;theta < PI ;theta = theta + (PI/meridians)){
   for(float phi=0;phi < 2*PI;phi= phi + (2*PI/parallels)){
         verts[index].x = cos(theta) * sin(phi);
         verts[index].y = sin(theta) * sin(phi);
         verts[index].z = cos(phi);
   }
}
*/













//have to be on the same plane
Canvas3DItem2 {
    var <>paths;
    var <>ambient=0.5; //number 0-1
    var <> specular=0.5;//number 0-1
    var <>diffuse=0.5;  //number 0-1
    var <>emissive; //color
    var <>shininess = 0.5;
    var <>width;
    var <>transforms;
    
    *new {
        ^super.new.init;
    }

    *cube {
        ^this.new.paths = #[
            [[-1,-1,-1],[1,-1,-1],[1, 1,-1],[-1, 1,-1],[-1,-1,-1]],
            [[-1,-1,-1],[-1,-1,1]],[[ 1,-1,-1],[ 1,-1,1]],[[-1, 1,-1],[-1, 1,1]],[[ 1, 1,-1],[ 1, 1,1]],
            [[-1,-1, 1],[1,-1, 1],[1, 1, 1],[-1, 1, 1],[-1,-1, 1]]
        ];
    }

    *grid {|n|
        var i=(n-1)/2;
        ^this.new.paths_(n.collect {|x| x=x/i-1; [[-1,x,0],[1,x,0]]} ++ n.collect {|x| x=x/i-1; [[x,-1,0],[x,1,0]]})
    }

    init {
        paths = [];
        width = 1;
        emissive = Color(0.3,0.6,0.1);
        transforms = [];
    }  
    
    transform {|matrix|
        paths = paths.collect {|p| p.collect {|v| Canvas3D2.vectorMatrixMul(v, matrix) }};
    }
}

Canvas3D2 : SCViewHolder {
    var <>items;
    var <>scale = 200;
    var <>perspective = 0.5;
    var <>distance = 2;
    var <>lightDir;
    var <>lightColor;
    var <>transforms;
    var <>preDrawFunc;
    var <>postDrawFunc;
    var animator;

    *new { |parent, bounds, items|
        ^super.new.init(parent, bounds, items);
    }
    
    project { |x0,y0,z0|
        var x, y, z, p;
        z = z0*perspective+distance;
        x = scale*(x0/z)+(this.bounds.width/2);
        y = scale*(y0/z)+(this.bounds.height/2);
        ^Point(x, y);    
    }

    init { |parent, bounds, argItems|
        items = argItems ? [];
        transforms = [];
        lightDir = RealVector3D[1.0,1.0,1.0].normalize;
        lightColor = Color(0.3,0.3,0.3);
        this.view = UserView(parent, bounds)
            .background_(Color.white)
            .drawFunc_({
                var viewPos = RealVector3D[0.0,0.0,1.0];
                /*transforms.inject( [0,0,1], { |s,t| 
                    Canvas3D2.vectorMatrixMul(s, t) 
                }).as(RealVector3D);*/
                var paths0 = items.collect{ |x| x.paths.collect{ |y| 
                        var transPath = y.collect{ |v|
                            (x.transforms ++ transforms).inject(v, { |s,t| Canvas3D2.vectorMatrixMul(s, t) });          
                        };
                        Tuple3(transPath,x,transPath[..(transPath.size-2)].sum/(transPath.size-1) ) } 
                    }.flatten;
                 var paths = paths0.sort{ |p1t,p2t|
                        var p1 = p1t.at1;
                        var p2 = p2t.at1;
                        (p1[..(p1.size-2)].sum/(p1.size-1)).as(RealVector3D).dist(viewPos) >
                            (p2[..(p2.size-2)].sum/(p2.size-1)).as(RealVector3D).dist(viewPos)
                    
                    };                
                 preDrawFunc.value;

                 paths.do { |pt,i|
                    var item = pt.at2;
                    var transPath = pt.at1;
          
                    var pathN = (transPath[1]-transPath[0].as(RealVector3D))
                        .cross( (transPath[2]-transPath[0]).as(RealVector3D) );
                    var dot = pathN <|> lightDir;
                    var dot2 =  pathN <|> (lightDir.as(RealVector3D) + RealVector3D[0.0, 0.0, 1.0] ).normalize;
                    var color = item.emissive
                        .add( lightColor * item.ambient)
                        .add( (lightColor * dot.abs * item.diffuse); )
                        .add( (lightColor * (dot2.abs**item.shininess) * item.specular) );
            
                    Pen.fillColor = color;
                    transPath.do {|v,i|
                        var x, y, z, p;
                        if(i==0) {
                            Pen.moveTo( this.project(*v) );
                        } {
                            Pen.lineTo( this.project(*v) );
                        };                                           		
                    };
                    Pen.fill;
   
                    
                };
                
                paths0.do{ |pt, i|
                    "path % is %".format(i,pt.at1).postln;
                    "path % has center %".format(i,pt.at3).postln;
                    "path % distance to view pos%".format(i,pt.at3.as(RealVector3D).dist(viewPos))
                };

                
                paths.do{ |pt|
                        var p = this.project(*pt.at3);
                        Pen.strokeColor = Color.black;
                        Pen.fillColor = pt.at2.emissive;
                        Pen.addArc(p, 4, 0, 2*pi);
                        Pen.fill;
                        Pen.addArc(p, 4, 0, 2*pi);
                        Pen.stroke;                
                };
                "view pos %".format(viewPos).postln;
                "closest object has color %".format( paths.last.at2.emissive.asArray ).postln;
                "closest object distance to view pos %".format( paths.last.at3.as(RealVector3D).dist(viewPos) ).postln;
                postDrawFunc.value;
            });
    }

    add {|item|
        items = items.add(item);
    }

    remove {|item|
        items.remove(item);
    }

    animate {|rate, func|
        animator.stop;
        if(rate.notNil and: {func.notNil}) {
            animator = Routine {
                var frame = 0;
                while {this.view.notNil and: {animator.notNil}} {
                    func.value(frame);
                    this.refresh;
                    (1/rate).wait;
                    frame = frame + 1;
                }
            }.play(AppClock);
        } {
            animator = nil;
        }
    }

    //matrix stuff by redFrik 050703
	*mIdentity {
		^[	#[1, 0, 0, 0],
			#[0, 1, 0, 0],
			#[0, 0, 1, 0],
			#[0, 0, 0, 1]];
	}
	*mTranslate {|tx, ty, tz|
		^[	#[1, 0, 0, 0],
			#[0, 1, 0, 0],
			#[0, 0, 1, 0],
			[tx, ty, tz, 1]];
	}
	*mScale {|sX, sY = (sX), sZ = (sX)|
		^[	[sX, 0, 0, 0],
			[0, sY, 0, 0],
			[0, 0, sZ, 0],
			#[0, 0, 0, 1]];
	}
	*mRotateX {|ax|
		^[	#[1, 0, 0, 0],
			[0, cos(ax), sin(ax), 0],
			[0, sin(ax).neg, cos(ax), 0],
			#[0, 0, 0, 1]];
	}
	*mRotateY {|ay|
		^[	[cos(ay), 0, sin(ay).neg, 0],
			#[0, 1, 0, 0],
			[sin(ay), 0, cos(ay), 0],
			#[0, 0, 0, 1]];
	}
	*mRotateZ {|az|
		^[	[cos(az), sin(az), 0, 0],
			[sin(az).neg, cos(az), 0, 0],
			#[0, 0, 1, 0],
			#[0, 0, 0, 1]];
	}
	
	*matrixMatrixMul {|matrix1, matrix2|
		var m0, m1, m2, m3;
		#m0, m1, m2, m3= matrix2;
		^Array.fill(4, {|x|
			Array.fill(4, {|y|
				(matrix1[x][0]*m0[y])+
				(matrix1[x][1]*m1[y])+
				(matrix1[x][2]*m2[y])+
				(matrix1[x][3]*m3[y])
			});
		});
	}
	*vectorMatrixMul {|vector, matrix|
		var v0, v1, v2, m0, m1, m2, m3;
		#v0, v1, v2= vector;
		#m0, m1, m2, m3= matrix;
		^[
			(v0*m0[0])+(v1*m1[0])+(v2*m2[0])+m3[0],
			(v0*m0[1])+(v1*m1[1])+(v2*m2[1])+m3[1],
			(v0*m0[2])+(v1*m1[2])+(v2*m2[2])+m3[2]
		];
	}
}

+ Color {
	* { arg v;
		^Color.fromArray( [red *v, green*v, blue*v, alpha] )
	}
}
