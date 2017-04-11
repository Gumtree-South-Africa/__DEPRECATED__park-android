package com.ebay.park.responses;

import android.text.TextUtils;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;

import java.util.Iterator;
import java.util.List;

public class ZipCodesResponse {

	private List<Result> results;
	private String status;

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static class Result {

		private List<AddressComponent> addressComponents;
		private String location;
		private String formattedAddress;
		private Geometry geometry;

		public String getFormattedAddress() {
			return formattedAddress;
		}

		public void setFormattedAddress(String formattedAddress) {
			this.formattedAddress = formattedAddress;
		}

		public Geometry getGeometry() {
			return geometry;
		}

		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}

		public List<AddressComponent> getAddressComponents() {
			return addressComponents;
		}

		public void setAddressComponents(List<AddressComponent> addressComponents) {
			this.addressComponents = addressComponents;
		}

		@Override
		public String toString() {
			// return formattedAddress;
			return getLocation();
		}

		private String getLocation() {
			if (TextUtils.isEmpty(location)) {
				AddressComponent address;
				String localityAddress = "";
				String stateAddress = "";
				Iterator<AddressComponent> iterator = addressComponents.iterator();
				while (iterator.hasNext()) {
					address = iterator.next();
					if (address.getTypes().contains("locality")) {
						localityAddress = address.getLong_name();
					} else if (address.getTypes().contains("sublocality_level_1")) {
						localityAddress = address.getLong_name();
					}
					if (address.getTypes().contains("administrative_area_level_1")) {
						stateAddress = address.getShort_name();
					} else if (stateAddress.isEmpty()) {
						stateAddress = "PR";// Puerto Rico
					}
				}
				location = String.format(ParkApplication.getInstance().getString(R.string.address_output_string),
						localityAddress, stateAddress);
			}
			return location;
		}

		public class Geometry {

			private Location location;

			public Location getLocation() {
				return location;
			}

			public void setLocation(Location location) {
				this.location = location;
			}

			public class Location {
				private Double lat;
				private Double lng;

				public Double getLat() {
					return lat;
				}

				public void setLat(Double lat) {
					this.lat = lat;
				}

				public Double getLng() {
					return lng;
				}

				public void setLng(Double lng) {
					this.lng = lng;
				}
			}
		}

		public class AddressComponent {

			private String long_name;
			private String short_name;
			private List<String> types;

			public String getLong_name() {
				return long_name;
			}

			public void setLong_name(String long_name) {
				this.long_name = long_name;
			}

			public String getShort_name() {
				return short_name;
			}

			public void setShort_name(String short_name) {
				this.short_name = short_name;
			}

			public List<String> getTypes() {
				return types;
			}

			public void setTypes(List<String> types) {
				this.types = types;
			}

		}

	}

}