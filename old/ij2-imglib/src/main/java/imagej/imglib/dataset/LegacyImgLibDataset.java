package imagej.imglib.dataset;

import imagej.Dimensions;
import imagej.MetaData;
import imagej.dataset.Dataset;
import imagej.dataset.PlanarDatasetFactory;
import imagej.dataset.RecursiveDataset;
import imagej.imglib.ImageUtils;
import imagej.imglib.TypeManager;
import imagej.process.Index;
import imagej.types.Type;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** This is an ImgLib aware Dataset. Constructor takes an imglib image and makes a dataset whose primitive access arrays match.
 * Also overrides the add/remove subset calls to set an invalid flag. Then user should always call its method called getImage() that returns
 * a cached Image and cached Image is recreated and populated with correct primitive access when invalid. User should not cache image from the
 * getImage() call.
 */
public class LegacyImgLibDataset implements Dataset, RecursiveDataset
{
	//************ instance variables ********************************************************
	
	private Dataset dataset;
	private Image<?> shadowImage;
	private Type ijType;
	private RealType<?> realType;

	//************ constructors ********************************************************
	
	/** create an ImgLibDataset from an existing ImgLib image */
	public LegacyImgLibDataset(Image<?> image)
	{
		this.shadowImage = image;
		
		this.realType = ImageUtils.getType(image);

		this.ijType = TypeManager.getIJType(realType);
		
		int[] dimensions = image.getDimensions();
		
		this.dataset = new PlanarDatasetFactory().createDataset(this.ijType, dimensions);

		if (dimensions.length < 2)
		{
			throw new IllegalArgumentException("ImgLibDataset cannot represent data of dimensionality < 2");
		}

		PlanarAccess<ArrayDataAccess<?>> access = ImageUtils.getPlanarAccess(this.shadowImage);
		
		if (dimensions.length == 2)  // TODO - could modify indexing code and subsetting code so that below loop works silently in 2d case
		{
			ArrayDataAccess<?> arrayAccess = access.getPlane(0);
			this.dataset.setData(arrayAccess.getCurrentStorageArray());
		}
		else // (dimensions.length > 2)
		{
			// make all Dataset's planes point at ImgLib's data arrays
			
			int subDimensionLength = dimensions.length-2;
		
			int[] position = Index.create(subDimensionLength);
			
			int[] origin = Index.create(subDimensionLength);
	
			int[] span = new int[subDimensionLength];
			for (int i = 0; i < subDimensionLength; i++)
				span[i] = dimensions[i+2];
			
			int planeNum = 0;
			
			while (Index.isValid(position, origin, span))
			{
				Dataset plane = this.dataset.getSubset(position);
				ArrayDataAccess<?> arrayAccess = access.getPlane(planeNum++);
				plane.setData(arrayAccess.getCurrentStorageArray());
				Index.increment(position, origin, span);
			}
		}
		
		// TODO - have a factory that takes a list of planerefs and builds a dataset without allocating data unnecessarily
	}

	/** create an ImgLibDataset from a Dataset. Calling imgLibDataset.getImage() will create an ImgLib image from the original Dataset. Another
	 * way to create an Image from a Dataset is found in ImageUtils::createShadowImage(). */
	public LegacyImgLibDataset(Dataset dataset)
	{
		this.shadowImage = null;
		
		this.ijType = dataset.getType();
		
		this.realType = TypeManager.getRealType(this.ijType);

		this.dataset = dataset;
	}
	
	//************ public interface ********************************************************

	@Override
	public int[] getDimensions()
	{
		return this.dataset.getDimensions();
	}

	@Override
	public Type getType()
	{
		return this.dataset.getType();
	}

	@Override
	public MetaData getMetaData()
	{
		return this.dataset.getMetaData();
	}

	@Override
	public void setMetaData(MetaData metadata)
	{
		this.dataset.setMetaData(metadata);
	}

	@Override
	public boolean isComposite()
	{
		return this.dataset.isComposite();
	}

	@Override
	public Dataset getParent()
	{
		return this.dataset.getParent();
	}

	@Override
	public void setParent(Dataset dataset)
	{
		this.dataset.setParent(dataset);
	}

	@Override
	public Object getData()
	{
		return this.dataset.getData();
	}

	@Override
	public void setData(Object data)
	{
		this.dataset.setData(data);
	}

	@Override
	public void releaseData()
	{
		this.dataset.releaseData();
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		this.shadowImage = null;
		return this.dataset.insertNewSubset(position);
	}

	@Override
	public Dataset removeSubset(int position)
	{
		this.shadowImage = null;
		return this.dataset.removeSubset(position);
	}

	@Override
	public Dataset getSubset(int position)
	{
		return this.dataset.getSubset(position);
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		return this.dataset.getSubset(index);
	}

	@Override
	public double getDouble(int[] position)
	{
		return this.dataset.getDouble(position);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		this.dataset.setDouble(position, value);
	}

	@Override
	public double getDouble(int[] index, int axis)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		return ds.getDouble(index, axis);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		ds.setDouble(index, axis, value);
	}

	@Override
	public long getLong(int[] position)
	{
		return this.dataset.getLong(position);
	}

	@Override
	public void setLong(int[] position, long value)
	{
		this.dataset.setLong(position, value);
	}

	@Override
	public long getLong(int[] index, int axis)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		return ds.getLong(index, axis);
	}

	@Override
	public void setLong(int[] index, int axis, long value)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		ds.setLong(index, axis, value);
	}

	@Override
	public Dataset getSubset(int[] partialIndex, int axis)
	{
		RecursiveDataset ds = (RecursiveDataset) this.dataset;
		
		return ds.getSubset(partialIndex, axis);
	}

	public Image<?> getImage()
	{
		if (this.shadowImage == null)
		{
			int[] dimensions = this.dataset.getDimensions();
			
			if (Dimensions.getTotalSamples(dimensions) == 0)
				throw new IllegalArgumentException("cannot create an ImgLibDataset which has one or more dimensions of size 0");
			
			this.shadowImage = ImageUtils.createShadowImage(this.dataset);
		}
		
		return this.shadowImage;
	}
}