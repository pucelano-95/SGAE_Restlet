package sgae.servidor.albumes;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Component;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.ClientList;

import sgae.nucleo.gruposMusicales.Album;
import sgae.nucleo.gruposMusicales.ControladorGruposMusicales;
import sgae.nucleo.gruposMusicales.ExcepcionGruposMusicales;
import sgae.nucleo.gruposMusicales.GrupoMusical;
import sgae.nucleo.personas.ExcepcionPersonas;
import sgae.servidor.aplicacion.SGAEaplicacion;
import sgae.util.generated.AlbumInfoBreve;
import sgae.util.generated.Albumes;
import sgae.util.generated.Link;

public class AlbumesServerResource extends ServerResource{
	
	SGAEaplicacion ref = (SGAEaplicacion)getApplication();
	ControladorGruposMusicales controladorGruposMusicales = ref.getControladorGruposMusicales();
	private String grupoID;
	
	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_PLAIN));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.grupoID = getAttribute("CIFgrupo");
		
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		Representation result = null;
		StringBuilder result2 = new StringBuilder();
	if (MediaType.TEXT_PLAIN.isCompatible(variant.getMediaType())) {		
		try {
			for (String album: controladorGruposMusicales.listarAlbumes(grupoID)) {
				result2.append((album == null) ? "" : album).append('\n');
			}
		} catch (ExcepcionGruposMusicales e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = new StringRepresentation(result2.toString());
	}
	
	else if (MediaType.TEXT_HTML.isCompatible(variant.getMediaType())) {
		Albumes albumes = new Albumes();	//XML
		//final List<AlbumInfoBreve> albumInfoBreve= albumesHTML.getAlbumInfoBreve();
		Map<String, Object> albumDataModel = new HashMap<String, Object>();
		

			try {
				for(sgae.nucleo.gruposMusicales.Album a: controladorGruposMusicales.recuperarAlbumes(this.grupoID) ) {
				
				AlbumInfoBreve albumInfo = new AlbumInfoBreve();	
				albumInfo.setTitulo(a.getTitulo());
				Link link = new Link();
				link.setHref(a.getId()+"/");
				link.setTitle("Albumes");
				link.setType("simple");
				albumInfo.setUri(link);
				albumes.getAlbumInfoBreve().add(albumInfo);
					}
				albumDataModel.put("Albumes", albumes);
			} catch (ExcepcionGruposMusicales e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		Representation albumesVtl = new ClientResource(
		LocalReference.createClapReference(getClass().getPackage())+ "/AlbumInfoBreve.vtl").get();
		// Wrap bean with Velocity representation
		return new TemplateRepresentation(albumesVtl, albumDataModel, MediaType.TEXT_HTML);

	}
		return result;
	}
	
	@Post("form")
	public Representation anadiralbum (Representation datos) throws ParseException, ExcepcionPersonas, ResourceException, ExcepcionGruposMusicales{

		Form form = new Form(datos);		
		String CIF= this.grupoID;
		String titulo= form.getFirstValue("TITULO");
		String fechaPublicacion= form.getFirstValue("FECHAPUBLICACION");
		int ejemplaresVendidos= Integer.parseInt(form.getFirstValue("EJEMPLARESVENDIDOS"));
		//TITULO=Ave Maria&FECHAPUBLICACION=02-04-1999&EJEMPLARESVENDIDOS=6
		 System.out.println("CIF: " + CIF);
		 System.out.println("Titulo: " + titulo);
		 System.out.println("Fecha de publicacion: " + fechaPublicacion);
		 System.out.println("Numero de ejemplares vendidos: " + ejemplaresVendidos);
		 Representation result = null;
		 
		try {
			controladorGruposMusicales.crearAlbum(CIF, titulo, fechaPublicacion, ejemplaresVendidos);
			// Si se produce la expcion significa que la persona ya existe --> el usuario quiere hacer un put de modificacion
			 result =  new StringRepresentation("CIF: " + CIF +" Titulo: " + titulo+" Fecha de publicacion: " + fechaPublicacion+" Numero de ejemplares vendidos: " + ejemplaresVendidos,   MediaType.TEXT_HTML);
		}catch (ParseException ax) {
			System.out.println("ParseException crear");
			 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
//		catch(ExcepcionPersonas ex) {
//	        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
//		}
		return result;

	}
	
	
	
}
