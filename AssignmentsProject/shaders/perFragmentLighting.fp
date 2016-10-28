
varying vec3 ecn;  // surface normal in camera 
varying vec3 ecv;  // surface fragment location in camera 
 
void main(void) {
	// TODO: do light computation here!
	vec3 ecnn = normalize(ecn);
    vec3 ecl = normalize( gl_LightSource[0].position.xyz - ecv.xyz ); // eye coordinates light vector
	vec4 color;
	color = gl_FrontMaterial.diffuse * max( 0.0, dot( ecnn, ecl ) );
	//color = color + gl_LightModel.ambient * gl_FrontMaterial.ambient;
	
	vec3 ech = normalize(ecl + normalize(-ecv));
	float val = max(0.0, dot(ecnn, ech));	
	color = color + pow( val, gl_FrontMaterial.shininess ) * gl_FrontMaterial.specular;
	
	gl_FragColor = vec4(color.xyz,1.0);
	
	// can use this to initially visualize the normal
    //gl_FragColor = vec4( ecn.xyz * 0.5 + vec3( 0.5, 0.5,0.5 ), 1.0 );
}
